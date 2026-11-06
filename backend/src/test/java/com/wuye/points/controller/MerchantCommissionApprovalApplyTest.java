package com.wuye.points.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.PriceApproval;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.PriceApprovalRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 价格审批「通过后真的生效」回归（前端 2026-11-06 19:10：抽佣审批通过后数据库好像没动）。
 *
 * <p>线上实测（8.147.60.8）：审批单 <code>pa_388bd774b01000</code>（商家分成 0.1）19:09:36 由物业领导通过，
 * 结果只写了 <code>merchants.individual_leader_rate=0.1000</code>——该列全仓库无人读取；
 * 分账真正用的 <code>merchant_property_configs.commission_rate</code> 仍是 0.0001，
 * 商家列表「抽佣比例」列也因响应里没有 <code>commissionRate</code> 而恒显示「—」，
 * 于是看起来就是「点了同意，什么都没发生」。本类把落库目标、两种值格式、配送费类型与出参可见性一起钉住。</p>
 */
@DisplayName("价格审批生效链路：抽佣/配送费落库 + 对象名 + 抽佣可见性")
class MerchantCommissionApprovalApplyTest extends ControllerTestBase {

    private static final String PC = "pc_apl00001";
    private static final String LEADER = "res_apl_leader";
    private static final String PLAIN = "res_apl_plain";
    private static final String MERCHANT = "mch_apl00001";
    private static final String CONFIG = "mpc_apl00001";
    private static final String PA_RATE = "pa_apl_rate01";
    private static final String PA_RATE_JSON = "pa_apl_rate02";
    private static final String PA_FEE = "pa_apl_fee01";
    private static final String PA_AD = "pa_apl_ad0001";
    private static final String PA_SHARE = "pa_apl_share01";

    @Autowired private ResidentRepository residentRepository;
    @Autowired private PropertyCompanyRepository propertyCompanyRepository;
    @Autowired private MerchantRepository merchantRepository;
    @Autowired private MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    @Autowired private PriceApprovalRepository priceApprovalRepository;

    @BeforeEach
    void setUp() {
        PropertyCompany company = new PropertyCompany();
        company.setId(PC);
        company.setName("审批生效测试物业");
        company.setStatus("active");
        propertyCompanyRepository.save(company);

        // 物业领导：role=property_admin 且未标 property_operator 子角色
        resident(LEADER, "13980000001", "物业领导", "property_admin");
        resident(PLAIN, "13980000002", "普通住户", "resident");

        Merchant merchant = new Merchant();
        merchant.setId(MERCHANT);
        merchant.setPlatformMerchantId("pm_apl00001");
        merchant.setPropertyCompanyId(PC);
        merchant.setName("元序测试店");
        merchant.setContactPhone("13980000100");
        merchant.setAuditStatus("approved");
        merchant.setStatus("active");
        merchant.setDeliveryFee(new BigDecimal("0.01"));
        merchantRepository.save(merchant);

        MerchantPropertyConfig config = new MerchantPropertyConfig();
        config.setId(CONFIG);
        config.setMerchantId(MERCHANT);
        config.setPropertyCompanyId(PC);
        config.setCommissionRate(new BigDecimal("0.0001"));
        config.setPointExchangeRate(new BigDecimal("100.0000"));
        config.setStatus("active");
        merchantPropertyConfigRepository.save(config);

        // 个体负责人接口落的是纯数字，管理端「发起申请」落的是 {"rate":x}，两种都必须能生效
        priceApprovalRepository.save(approval(PA_RATE, "merchant_distribution", MERCHANT, "0.10"));
        priceApprovalRepository.save(approval(PA_RATE_JSON, "merchant_distribution", MERCHANT, "{\"rate\":0.25}"));
        priceApprovalRepository.save(approval(PA_FEE, "delivery_fee", MERCHANT, "{\"amount\":1.50}"));
        priceApprovalRepository.save(approval(PA_AD, "merchant_ad", MERCHANT, "{\"amount\":9.90}"));
        priceApprovalRepository.save(approval(PA_SHARE, "resident_share_rate", PC, "{\"rate\":0.10}"));
    }

    @AfterEach
    void cleanUp() {
        priceApprovalRepository.deleteAllById(
                List.of(PA_RATE, PA_RATE_JSON, PA_FEE, PA_AD, PA_SHARE));
        merchantPropertyConfigRepository.deleteById(CONFIG);
        merchantRepository.deleteById(MERCHANT);
        residentRepository.deleteAllById(List.of(PLAIN, LEADER));
        propertyCompanyRepository.deleteById(PC);
    }

    // ==================== 抽佣审批落库 ====================

    @Test
    @DisplayName("抽佣审批（纯数字 0.10）通过：merchant_property_configs.commission_rate 变为 0.10")
    void approve_plainRate_landsOnCommissionConfig() throws Exception {
        mockMvc.perform(put("/admin/price-approvals/" + PA_RATE)
                        .header("Authorization", bearer(LEADER, "property_admin", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":true,\"remark\":\"同意\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("approved"));

        assertEquals(0, new BigDecimal("0.1000")
                        .compareTo(config().getCommissionRate()),
                "抽佣审批必须改分账真正读取的 commission_rate，实际=" + config().getCommissionRate());
        assertNull(merchant().getIndividualLeaderRate(),
                "不再写无读取方的 merchants.individual_leader_rate");
    }

    @Test
    @DisplayName("抽佣审批（管理端格式 {\"rate\":0.25}）通过：同样生效（此前解析不了直接 400）")
    void approve_jsonRate_landsToo() throws Exception {
        mockMvc.perform(put("/admin/price-approvals/" + PA_RATE_JSON)
                        .header("Authorization", bearer(LEADER, "property_admin", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":true}"))
                .andExpect(status().isOk());

        assertEquals(0, new BigDecimal("0.2500").compareTo(config().getCommissionRate()));
    }

    @Test
    @DisplayName("抽佣比例越界（1.5）：400 且审批保持 pending、抽佣不变")
    void approve_rateOutOfRange_rollsBack() throws Exception {
        priceApprovalRepository.findById(PA_RATE).ifPresent(pa -> {
            pa.setNewValue("1.5");
            priceApprovalRepository.save(pa);
        });

        mockMvc.perform(put("/admin/price-approvals/" + PA_RATE)
                        .header("Authorization", bearer(LEADER, "property_admin", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":true}"))
                .andExpect(status().isBadRequest());

        assertEquals("pending", priceApprovalRepository.findById(PA_RATE).orElseThrow().getStatus());
        assertEquals(0, new BigDecimal("0.0001").compareTo(config().getCommissionRate()));
    }

    // ==================== 配送费审批落库 ====================

    @Test
    @DisplayName("配送费审批 {\"amount\":1.50} 通过：merchants.delivery_fee 由 0.01 变 1.50（此前只改状态）")
    void approve_deliveryFee_lands() throws Exception {
        mockMvc.perform(put("/admin/price-approvals/" + PA_FEE)
                        .header("Authorization", bearer(LEADER, "property_admin", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":true}"))
                .andExpect(status().isOk());

        assertEquals(0, new BigDecimal("1.50").compareTo(merchant().getDeliveryFee()),
                "配送费审批必须改 merchants.delivery_fee，实际=" + merchant().getDeliveryFee());
    }

    @Test
    @DisplayName("尚无落库实现的类型（merchant_ad）：审批照常通过、不报错、不乱写商家参数")
    void approve_typeWithoutApply_marksApprovedOnly() throws Exception {
        mockMvc.perform(put("/admin/price-approvals/" + PA_AD)
                        .header("Authorization", bearer(LEADER, "property_admin", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":true}"))
                .andExpect(status().isOk());

        assertEquals("approved", priceApprovalRepository.findById(PA_AD).orElseThrow().getStatus());
        assertEquals(0, new BigDecimal("0.01").compareTo(merchant().getDeliveryFee()));
        assertEquals(0, new BigDecimal("0.0001").compareTo(config().getCommissionRate()));
    }

    // ==================== 对象列名字 ====================

    @Test
    @DisplayName("审批列表「对象」返回 itemName：商家名 / 物业公司名，itemId 仍保留")
    void approvalList_exposesItemName() throws Exception {
        JsonNode list = dataOf(mockMvc.perform(get("/admin/price-approvals")
                        .header("Authorization", bearer(LEADER, "property_admin", PC, null))
                        .param("page", "1").param("pageSize", "20"))
                .andReturn())
                .path("list");

        assertEquals("元序测试店", itemNameOf(list, PA_RATE));
        assertEquals("元序测试店", itemNameOf(list, PA_FEE));
        assertEquals("审批生效测试物业", itemNameOf(list, PA_SHARE));
        for (JsonNode node : list) {
            assertTrue(node.has("itemId") && !node.path("itemId").asText().isEmpty(),
                    "itemId 必须保留，前端要按它跳转；该行=" + node);
        }
    }

    // ==================== 抽佣可见性（经营敏感） ====================

    @Test
    @DisplayName("商家列表：物业领导/负责人拿得到 commissionRate 值，普通住户拿到 null（线上 non_null 序列化时该键缺失）")
    void merchantList_commissionVisibility() throws Exception {
        JsonNode leaderRow = firstMerchantRowOf(LEADER, "property_admin");
        assertFalse(leaderRow.path("commissionRate").isNull(),
                "管理侧必须能看到抽佣比例，否则审批后仍像「没动」；该行=" + leaderRow);
        assertEquals(0, new BigDecimal("0.0001")
                .compareTo(new BigDecimal(leaderRow.path("commissionRate").asText())));
        assertEquals("元序测试店", leaderRow.path("name").asText());

        JsonNode residentRow = firstMerchantRowOf(PLAIN, "resident");
        assertTrue(residentRow.path("commissionRate").isNull(),
                "抽佣比例不得下发给普通住户，该行=" + residentRow);
    }

    // ==================== 夹具 ====================

    private PriceApproval approval(String id, String itemType, String itemId, String newValue) {
        PriceApproval approval = new PriceApproval();
        approval.setId(id);
        approval.setPropertyCompanyId(PC);
        approval.setApplicantId(LEADER);
        approval.setApplicantRole("property_admin");
        approval.setItemType(itemType);
        approval.setItemId(itemId);
        approval.setOldValue(null);
        approval.setNewValue(newValue);
        approval.setStatus("pending");
        return approval;
    }

    private MerchantPropertyConfig config() {
        return merchantPropertyConfigRepository.findById(CONFIG).orElseThrow();
    }

    private Merchant merchant() {
        return merchantRepository.findById(MERCHANT).orElseThrow();
    }

    private JsonNode dataOf(MvcResult result) throws Exception {
        return objectMapper.readTree(contentOf(result)).path("data");
    }

    private JsonNode firstMerchantRowOf(String userId, String role) throws Exception {
        JsonNode list = dataOf(mockMvc.perform(get("/merchants")
                        .header("Authorization", bearer(userId, role, PC, null))
                        .param("page", "1").param("pageSize", "20"))
                .andReturn())
                .path("list");
        for (JsonNode node : list) {
            if (MERCHANT.equals(node.path("id").asText())) {
                return node;
            }
        }
        throw new AssertionError("商家列表未返回测试商家：" + list);
    }

    private String itemNameOf(JsonNode list, String approvalId) {
        for (JsonNode node : list) {
            if (approvalId.equals(node.path("id").asText())) {
                return node.path("itemName").asText(null);
            }
        }
        throw new AssertionError("审批列表缺少记录 " + approvalId);
    }

    private void resident(String id, String phone, String name, String role) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setPhone(phone);
        resident.setName(name);
        resident.setRole(role);
        resident.setStatus("active");
        resident.setPropertyCompanyId(PC);
        resident.setCommunityId("com_apl0001");
        resident.setBuilding("1");
        resident.setUnit("1");
        resident.setRoom("101");
        resident.setPointBalance(0);
        resident.setCoinBalance(BigDecimal.ZERO);
        resident.setTokenVersion(0);
        residentRepository.save(resident);
    }

    private String contentOf(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }
}
