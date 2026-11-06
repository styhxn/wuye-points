package com.wuye.points.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantCommunityDistance;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.Product;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.MerchantCommunityDistanceRepository;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.ProductRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 配送费区间自由配置（2026-11-06 需求 3）。
 *
 * <p>原计划按距离自动算运费，老板改成「让他们自己去设定」。落到后端就是：
 * 商家在 {@code merchants.delivery_fee_tiers} 里自由配多档区间（每档可选维度
 * amount=按订单商品金额 / km=按商家到小区的距离），<b>一档都没命中就回落到
 * {@code merchants.delivery_fee} 这套默认值</b>。此前该字段只有校验+回显、运费计算从不读取，
 * 现网 3 家已填的档位实际不生效。</p>
 *
 * <p>满额减免压在档位之上：先由档位定出原价，减免开关打开且达门槛才归零；
 * 档位本身就是 0 元时<b>不算减免</b>，否则会把档位差错记成商家补贴、结算真扣商家钱。</p>
 */
@DisplayName("配送费区间：自由档位 + 默认值回落")
class DeliveryFeeTierConfigTest extends ControllerTestBase {

    private static final String PC = "pc_tier0001";
    private static final String COM = "com_tier0001";
    private static final String RESIDENT = "res_tier_buyer";
    private static final String MERCHANT_OWNER = "res_tier_owner";
    private static final String MERCHANT = "mch_tier0001";

    @Autowired private PropertyCompanyRepository propertyCompanyRepository;
    @Autowired private ResidentRepository residentRepository;
    @Autowired private MerchantRepository merchantRepository;
    @Autowired private MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    @Autowired private MerchantCommunityDistanceRepository distanceRepository;
    @Autowired private ProductRepository productRepository;

    @BeforeEach
    void seed() {
        PropertyCompany company = new PropertyCompany();
        company.setId(PC);
        company.setName("档位测试物业");
        company.setStatus("active");
        propertyCompanyRepository.save(company);

        resident(RESIDENT, "13910000001", "resident");
        resident(MERCHANT_OWNER, "13910000002", "merchant");

        Merchant merchant = new Merchant();
        merchant.setId(MERCHANT);
        merchant.setPlatformMerchantId("pmm_" + MERCHANT);
        merchant.setPropertyCompanyId(PC);
        merchant.setName("档位测试店");
        merchant.setCategory("外卖小吃");
        merchant.setAuditStatus("approved");
        merchant.setStatus("active");
        merchant.setMerchantSource("platform");
        merchant.setResidentId(MERCHANT_OWNER);
        merchant.setDeliveryFee(new BigDecimal("5.00"));   // 默认值：未命中任何档位时收这个
        merchantRepository.save(merchant);

        MerchantPropertyConfig config = new MerchantPropertyConfig();
        config.setId("mpc_tier0001");
        config.setMerchantId(MERCHANT);
        config.setPropertyCompanyId(PC);
        config.setStatus("active");
        config.setCommissionRate(new BigDecimal("0.0500"));
        config.setPointExchangeRate(new BigDecimal("100.0000"));
        merchantPropertyConfigRepository.save(config);

        Product product = new Product();
        product.setId("prd_tier0001");
        product.setMerchantId(MERCHANT);
        product.setName("档位测试商品");
        product.setCategory("外卖小吃");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(100);
        product.setStatus("active");
        productRepository.save(product);
    }

    @AfterEach
    void cleanUp() {
        distanceRepository.deleteAll(distanceRepository.findByMerchantId(MERCHANT));
        productRepository.deleteById("prd_tier0001");
        merchantPropertyConfigRepository.deleteById("mpc_tier0001");
        merchantRepository.deleteById(MERCHANT);
        residentRepository.deleteAllById(List.of(RESIDENT, MERCHANT_OWNER));
        propertyCompanyRepository.deleteById(PC);
    }

    private void resident(String id, String phone, String role) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setPhone(phone);
        resident.setName("档位测试" + id.substring(4));
        resident.setRole(role);
        resident.setStatus("active");
        resident.setPropertyCompanyId(PC);
        resident.setCommunityId(COM);
        resident.setBuilding("1");
        resident.setUnit("1");
        resident.setRoom("101");
        resident.setPointBalance(0);
        resident.setCoinBalance(BigDecimal.ZERO);
        resident.setTokenVersion(0);
        residentRepository.save(resident);
    }

    /** 直接写库配置商家档位与减免开关，避免依赖配置接口本身。 */
    private void configure(String tiersJson, Boolean waiverEnabled, String waiverThreshold) {
        Merchant merchant = merchantRepository.findById(MERCHANT).orElseThrow();
        merchant.setDeliveryFeeTiers(tiersJson);
        merchant.setFreeDeliveryEnabled(waiverEnabled);
        merchant.setFreeDeliveryThreshold(waiverThreshold == null ? null : new BigDecimal(waiverThreshold));
        merchantRepository.save(merchant);
    }

    private void seedDistance(String communityId, String km) {
        MerchantCommunityDistance distance = new MerchantCommunityDistance();
        distance.setId("mcd_" + communityId);
        distance.setMerchantId(MERCHANT);
        distance.setCommunityId(communityId);
        distance.setPropertyCompanyId(PC);
        distance.setDistanceKm(new BigDecimal(km));
        distanceRepository.save(distance);
    }

    private JsonNode preview(int quantity) throws Exception {
        return objectMapper.readTree(contentOf(mockMvc.perform(post("/orders/preview")
                        .header("Authorization", bearer(RESIDENT, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"merchantId\":\"" + MERCHANT + "\",\"items\":[{\"productId\":\"prd_tier0001\","
                                + "\"quantity\":" + quantity + "}]}"))
                .andExpect(status().isOk())
                .andReturn()));
    }

    private String contentOf(org.springframework.test.web.servlet.MvcResult result)
            throws java.io.UnsupportedEncodingException {
        return result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
    }

    private void assertAmount(JsonNode node, String field, String expected, String hint) {
        assertEquals(0, new BigDecimal(expected).compareTo(node.path(field).decimalValue()),
                hint + "，" + field + "=" + node.path(field).asText() + "，完整=" + node);
    }

    @Test
    @DisplayName("金额档位命中：0-20 元收 3 元、20 元以上收 1 元")
    void amountTiers_areApplied() throws Exception {
        configure("[{\"metric\":\"amount\",\"min\":0,\"max\":20,\"fee\":3.00,\"enabled\":true},"
                + "{\"metric\":\"amount\",\"min\":20,\"max\":null,\"fee\":1.00,\"enabled\":true}]", null, null);

        JsonNode light = preview(1).path("data");     // 10 元 → 0-20 档
        assertAmount(light, "deliveryFee", "3.00", "10 元订单应命中 0-20 档收 3 元");
        assertAmount(light, "originalDeliveryFee", "3.00", "档位价即原价，不该拿默认 5 元当原价");
        assertAmount(light, "deliveryWaiverAmount", "0", "未开减免不应产生减免额");

        JsonNode heavy = preview(3).path("data");     // 30 元 → 20 以上档
        assertAmount(heavy, "deliveryFee", "1.00", "30 元订单应命中 20 以上档收 1 元");
    }

    @Test
    @DisplayName("未命中任何档位：回落到默认配送费 5.00")
    void unmatchedTier_fallsBackToDefault() throws Exception {
        configure("[{\"metric\":\"amount\",\"min\":0,\"max\":15,\"fee\":2.00,\"enabled\":true}]", null, null);

        JsonNode data = preview(3).path("data");      // 30 元超出唯一档位上限
        assertAmount(data, "deliveryFee", "5.00", "未命中必须回落默认值");
        assertAmount(data, "originalDeliveryFee", "5.00", "回落值同样就是原价快照");
    }

    @Test
    @DisplayName("距离档位：按「商家×小区」已配距离命中；没配距离的小区回落默认值")
    void kmTiers_useConfiguredDistance() throws Exception {
        configure("[{\"minKm\":0,\"maxKm\":3,\"fee\":4.00,\"enabled\":true},"
                + "{\"minKm\":3,\"maxKm\":null,\"fee\":8.00,\"enabled\":true}]", null, null);
        seedDistance(COM, "2.50");

        assertAmount(preview(1).path("data"), "deliveryFee", "4.00", "2.5km 应命中 0-3km 档");

        // 买家换到没配距离的小区：距离未知 → 不命中 → 回落默认，而不是报错或收 0 元
        Resident buyer = residentRepository.findById(RESIDENT).orElseThrow();
        buyer.setCommunityId("com_unknown01");
        residentRepository.save(buyer);
        assertAmount(preview(1).path("data"), "deliveryFee", "5.00", "无距离配置应回落默认值");
    }

    @Test
    @DisplayName("满额减免压在档位之上：档位价 3.00 达门槛后归零，减免额=3.00")
    void waiverAppliesOnTopOfTier() throws Exception {
        configure("[{\"metric\":\"amount\",\"min\":0,\"max\":20,\"fee\":3.00,\"enabled\":true}]",
                true, "10.00");

        JsonNode data = preview(1).path("data");      // 10 元：命中档位 3.00，且达减免门槛
        assertAmount(data, "deliveryFee", "0", "达门槛应免掉档位价");
        assertAmount(data, "originalDeliveryFee", "3.00", "原价是档位价，不是默认 5.00");
        assertAmount(data, "deliveryWaiverAmount", "3.00", "减免额只等于被免掉的档位价");
        assertEquals(true, data.path("freeDeliveryEligible").asBoolean());
    }

    @Test
    @DisplayName("档位本身就是 0 元：不算减免，避免把档位差错记成商家补贴")
    void zeroFeeTier_isNotRecordedAsSubsidy() throws Exception {
        configure("[{\"metric\":\"amount\",\"min\":0,\"max\":20,\"fee\":0,\"enabled\":true}]",
                true, "10.00");

        JsonNode data = preview(1).path("data");
        assertAmount(data, "deliveryFee", "0", "0 元档位直接收 0");
        assertAmount(data, "originalDeliveryFee", "0", "原价同为 0");
        assertAmount(data, "deliveryWaiverAmount", "0",
                "不能把 0 元档位当成减免，否则结算会按 sponsor=merchant 扣商家钱");
    }

    @Test
    @DisplayName("配置校验：同维度重叠、非法维度、上界不大于下界、全关档，一律 400")
    void tierValidation_rejectsBadConfigs() throws Exception {
        for (String body : List.of(
                // 同维度区间重叠
                "{\"deliveryFeeTiers\":[{\"metric\":\"amount\",\"min\":0,\"max\":20,\"fee\":3,\"enabled\":true},"
                        + "{\"metric\":\"amount\",\"min\":10,\"max\":30,\"fee\":2,\"enabled\":true}]}",
                // 非法维度
                "{\"deliveryFeeTiers\":[{\"metric\":\"weight\",\"min\":0,\"max\":20,\"fee\":3,\"enabled\":true}]}",
                // 上界不大于下界
                "{\"deliveryFeeTiers\":[{\"metric\":\"amount\",\"min\":20,\"max\":10,\"fee\":3,\"enabled\":true}]}",
                // 一个启用档都没有
                "{\"deliveryFeeTiers\":[{\"metric\":\"amount\",\"min\":0,\"max\":20,\"fee\":3,\"enabled\":false}]}")) {
            mockMvc.perform(put("/merchants/my/delivery-fees")
                            .header("Authorization", bearer(MERCHANT_OWNER, "merchant", PC, null))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        // 不同维度并存、且边界相邻（0-20 与 20-不限）必须通过
        mockMvc.perform(put("/merchants/my/delivery-fees")
                        .header("Authorization", bearer(MERCHANT_OWNER, "merchant", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deliveryFeeTiers\":["
                                + "{\"metric\":\"amount\",\"min\":0,\"max\":20,\"fee\":3,\"enabled\":true},"
                                + "{\"metric\":\"amount\",\"min\":20,\"max\":null,\"fee\":1,\"enabled\":true},"
                                + "{\"minKm\":0,\"maxKm\":3,\"fee\":4,\"enabled\":true}]}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("档位配置可回显：商家详情返回 metric/min/max 与历史 minKm/maxKm")
    void tiersRoundTrip() throws Exception {
        configure("[{\"metric\":\"amount\",\"min\":0,\"max\":20,\"fee\":3.00,\"enabled\":true}]", null, null);

        mockMvc.perform(get("/merchants/" + MERCHANT)
                        .header("Authorization", bearer(MERCHANT_OWNER, "merchant", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deliveryFeeTiers[0].metric").value("amount"))
                .andExpect(jsonPath("$.data.deliveryFeeTiers[0].min").value(0))
                .andExpect(jsonPath("$.data.deliveryFeeTiers[0].max").value(20))
                .andExpect(jsonPath("$.data.deliveryFeeTiers[0].fee").value(3.0));
    }
}
