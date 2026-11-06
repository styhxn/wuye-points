package com.wuye.points.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.Product;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.OrderRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 满额减免开关回归（2026-11-06 前端反馈「配送费满减未开启仍直接减了」）。
 *
 * <p>根因：{@code merchants.free_delivery_enabled} 全仓只有写入、没有任何读取，
 * 减免与否只由 {@code free_delivery_threshold} 决定。门槛填 0.00 时「满 0 元免运费」恒成立，
 * 于是商家在管理端把开关关掉也照样免——线上实证商家「元序」(mch_386992df981000)
 * {@code enabled=0, threshold=0.00, delivery_fee=0.01}，当天四笔单全部被免掉 0.01 元。</p>
 *
 * <p>同一条链路上还有第二个「配置不生效」：减免承担方一直硬编码成 {@code merchant}，
 * 商家配置的 {@code free_delivery_sponsor}（none/merchant/property/platform）从不参与快照，
 * 而结算侧只认 {@code merchant} 才扣商家，配成平台出资会被错记到商家头上。一并修。</p>
 */
@DisplayName("配送费满额减免：开关与承担方")
class DeliveryFeeWaiverSwitchTest extends ControllerTestBase {

    private static final String PC = "pc_fee00001";
    private static final String RESIDENT = "res_fee0001";
    private static final String MERCHANT = "mch_fee0001";

    @Autowired private PropertyCompanyRepository propertyCompanyRepository;
    @Autowired private ResidentRepository residentRepository;
    @Autowired private MerchantRepository merchantRepository;
    @Autowired private MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;

    @BeforeEach
    void seed() {
        PropertyCompany company = new PropertyCompany();
        company.setId(PC);
        company.setName("配送费测试物业");
        company.setStatus("active");
        propertyCompanyRepository.save(company);

        Resident resident = new Resident();
        resident.setId(RESIDENT);
        resident.setName("配送费测试住户");
        resident.setPhone("13940000001");
        resident.setRole("resident");
        resident.setStatus("active");
        resident.setPropertyCompanyId(PC);
        resident.setCommunityId("com_fee0001");
        resident.setBuilding("1");
        resident.setUnit("1");
        resident.setRoom("101");
        resident.setPointBalance(0);
        resident.setCoinBalance(BigDecimal.ZERO);
        resident.setTokenVersion(0);
        residentRepository.save(resident);

        MerchantPropertyConfig config = new MerchantPropertyConfig();
        config.setId("mpc_fee0001");
        config.setMerchantId(MERCHANT);
        config.setPropertyCompanyId(PC);
        config.setStatus("active");
        config.setCommissionRate(new BigDecimal("0.0500"));
        config.setPointExchangeRate(new BigDecimal("100.0000"));
        merchantPropertyConfigRepository.save(config);

        productRepository.save(product("prd_fee_normal", "普通商品", "外卖小吃", "10.00"));
        productRepository.save(product("prd_fee_group", "团购商品", "团购", "10.00"));
    }

    @AfterEach
    void cleanUp() {
        for (Order order : orderRepository.findAll()) {
            if (MERCHANT.equals(order.getMerchantId())) {
                orderRepository.delete(order);
            }
        }
        productRepository.deleteAllById(List.of("prd_fee_normal", "prd_fee_group"));
        merchantPropertyConfigRepository.deleteById("mpc_fee0001");
        merchantRepository.deleteById(MERCHANT);
        residentRepository.deleteById(RESIDENT);
        propertyCompanyRepository.deleteById(PC);
    }

    private Product product(String id, String name, String category, String price) {
        Product product = new Product();
        product.setId(id);
        product.setMerchantId(MERCHANT);
        product.setName(name);
        product.setCategory(category);
        product.setPrice(new BigDecimal(price));
        product.setStock(100);
        product.setStatus("active");
        return product;
    }

    /** 按用例给商家配置：配送费、门槛、开关、承担方。 */
    private void configureMerchant(String deliveryFee, String threshold, Boolean enabled, String sponsor) {
        Merchant merchant = new Merchant();
        merchant.setId(MERCHANT);
        merchant.setPlatformMerchantId("pmm_" + MERCHANT);
        merchant.setPropertyCompanyId(PC);
        merchant.setName("配送费测试店");
        merchant.setCategory("外卖小吃");
        merchant.setAuditStatus("approved");
        merchant.setStatus("active");
        merchant.setMerchantSource("platform");
        merchant.setDeliveryFee(new BigDecimal(deliveryFee));
        merchant.setFreeDeliveryThreshold(threshold == null ? null : new BigDecimal(threshold));
        merchant.setFreeDeliveryEnabled(enabled);
        merchant.setFreeDeliverySponsor(sponsor);
        merchantRepository.save(merchant);
    }

    private String contentOf(org.springframework.test.web.servlet.MvcResult result)
            throws java.io.UnsupportedEncodingException {
        return result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
    }

    private JsonNode preview(String productIds, int quantity) throws Exception {
        return objectMapper.readTree(contentOf(mockMvc.perform(post("/orders/preview")
                        .header("Authorization", bearer(RESIDENT, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"merchantId\":\"" + MERCHANT + "\",\"items\":[{\"productId\":\""
                                + productIds + "\",\"quantity\":" + quantity + "}]}"))
                .andExpect(status().isOk())
                .andReturn()));
    }

    private JsonNode createOrder(String productId, int quantity) throws Exception {
        return objectMapper.readTree(contentOf(mockMvc.perform(post("/orders")
                        .header("Authorization", bearer(RESIDENT, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"merchantId\":\"" + MERCHANT + "\",\"items\":[{\"productId\":\""
                                + productId + "\",\"quantity\":" + quantity + "}]}"))
                .andExpect(status().isOk())
                .andReturn()));
    }

    @Test
    @DisplayName("开关关闭 + 门槛 0.00：不再免配送费（线上「元序」复现）")
    void switchOffWithZeroThreshold_stillChargesDeliveryFee() throws Exception {
        configureMerchant("0.01", "0.00", false, "merchant");

        JsonNode data = preview("prd_fee_normal", 1).path("data");
        assertEquals(0, new BigDecimal("0.01").compareTo(data.path("deliveryFee").decimalValue()),
                "关掉开关后必须照收配送费，实际=" + data);
        assertEquals(0, data.path("deliveryWaiverAmount").decimalValue().compareTo(BigDecimal.ZERO),
                "不应产生减免额");
        assertEquals(false, data.path("freeDeliveryEligible").asBoolean());

        String orderId = createOrder("prd_fee_normal", 1).path("data").path("orderId").asText();
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertEquals(0, new BigDecimal("0.01").compareTo(order.getDeliveryFee()));
        assertEquals(0, BigDecimal.ZERO.compareTo(order.getDeliverySubsidyAmount()));
        assertEquals("none", order.getDeliverySubsidySponsor());
    }

    @Test
    @DisplayName("开关打开且达到门槛：照常减免，承担方按商家配置")
    void switchOnAndThresholdMet_waivesWithConfiguredSponsor() throws Exception {
        configureMerchant("2.00", "20.00", true, "merchant");

        JsonNode data = preview("prd_fee_normal", 2).path("data");
        assertEquals(0, BigDecimal.ZERO.compareTo(data.path("deliveryFee").decimalValue()), "满 20 应免运费");
        assertEquals(0, new BigDecimal("2.00").compareTo(data.path("deliveryWaiverAmount").decimalValue()));
        assertEquals(true, data.path("freeDeliveryEligible").asBoolean());

        String orderId = createOrder("prd_fee_normal", 2).path("data").path("orderId").asText();
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertEquals(0, BigDecimal.ZERO.compareTo(order.getDeliveryFee()));
        assertEquals(0, new BigDecimal("2.00").compareTo(order.getDeliverySubsidyAmount()));
        assertEquals("merchant", order.getDeliverySubsidySponsor());
    }

    @Test
    @DisplayName("开关从未设置（历史 null）：按未开启处理，不减免")
    void switchNeverSet_isTreatedAsOff() throws Exception {
        configureMerchant("0.10", "30.00", null, "merchant");

        JsonNode data = preview("prd_fee_normal", 5).path("data");
        assertEquals(0, new BigDecimal("0.10").compareTo(data.path("deliveryFee").decimalValue()),
                "开关没打开过就不该免，实际=" + data);
        assertEquals(false, data.path("freeDeliveryEligible").asBoolean());
    }

    @Test
    @DisplayName("承担方配置为平台：快照记 platform，不再被硬编码成 merchant 错扣商家")
    void sponsorConfiguredPlatform_isRecordedAsPlatform() throws Exception {
        configureMerchant("1.00", "10.00", true, "platform");

        String orderId = createOrder("prd_fee_normal", 2).path("data").path("orderId").asText();
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertEquals(0, BigDecimal.ZERO.compareTo(order.getDeliveryFee()));
        assertEquals("platform", order.getDeliverySubsidySponsor(),
                "结算侧只认 merchant 才扣商家，写错会让平台出资的减免错扣到商家头上");
    }

    @Test
    @DisplayName("团购单免运费不受开关影响（条目14 口径保持）")
    void groupBuyRemainsFreeRegardlessOfSwitch() throws Exception {
        configureMerchant("5.00", null, false, "merchant");

        JsonNode data = preview("prd_fee_group", 1).path("data");
        assertEquals(0, BigDecimal.ZERO.compareTo(data.path("deliveryFee").decimalValue()),
                "整单团购仍应免运费，与实际=" + data);
        assertEquals(0, BigDecimal.ZERO.compareTo(data.path("totalAmount").decimalValue()
                .subtract(new BigDecimal("10.00"))));
    }

    @Test
    @DisplayName("购物车运费预估同样认开关：关闭时不再显示「已减免」")
    void cartDeliveryFeeRespectsSwitch() throws Exception {
        configureMerchant("2.00", "20.00", false, "merchant");

        mockMvc.perform(post("/cart/delivery-fee")
                        .header("Authorization", bearer(RESIDENT, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"merchantId\":\"" + MERCHANT + "\","
                                + "\"items\":[{\"productId\":\"prd_fee_normal\",\"quantity\":3}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deliveryFee").value(
                        org.hamcrest.Matchers.closeTo(2.0, 0.001)))
                .andExpect(jsonPath("$.data.freeDeliveryEligible").value(false));
    }
}
