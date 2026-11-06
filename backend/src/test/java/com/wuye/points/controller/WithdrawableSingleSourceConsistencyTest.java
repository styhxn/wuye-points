package com.wuye.points.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.wuye.points.entity.Coordinator;
import com.wuye.points.entity.DistributionRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.PropertyManager;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.WalletFlow;
import com.wuye.points.entity.WechatSplitRecord;
import com.wuye.points.repository.CoordinatorRepository;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.PropertyManagerRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.WalletFlowRepository;
import com.wuye.points.repository.WechatSplitRecordRepository;
import com.wuye.points.repository.WithdrawalRecordRepository;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 可提现余额「口径唯一」回归（前端 2026-11-06 需求 P0-1 / P0-2 / P0-3）。
 *
 * <p>钉住三件事：</p>
 * <ol>
 *   <li>分账所得页 {@code /distribution/earnings/mine} 返回的 {@code withdrawableAmount}
 *       与提现接口校验用的是同一个数——页面显示多少就提得出多少，不会再出现 94003/94006；</li>
 *   <li>微信分账已实时到账的金额，展示端与校验端同时扣减
 *       （此前物业经理钱包概览漏扣，页面显示 10.00、提现只允许 6.00）；</li>
 *   <li>余额类接口在提交前就返回手续费率，前端可自行展示「预计到账」。</li>
 * </ol>
 */
@DisplayName("可提现口径唯一：分账所得页 / 提现校验 / 手续费率")
class WithdrawableSingleSourceConsistencyTest extends ControllerTestBase {

    private static final String PC = "pc_wsl00001";
    private static final String COO = "res_wsl_coo";
    private static final String COO_ID = "coo_wsl0001";
    private static final String MGR = "res_wsl_mgr";
    private static final String MGR_ID = "pm_wsl0001";
    private static final String DR = "dr_wsl00001";
    private static final String ORDER = "ord_wsl00001";
    private static final String FLOW_PM = "wf_wsl_pm001";
    private static final String FLOW_RES = "wf_wsl_res001";
    private static final String SPLIT_PM = "wsr_wsl_pm001";

    private static final BigDecimal FEE_RATE = new BigDecimal("0.0060");

    @Autowired private ResidentRepository residentRepository;
    @Autowired private PropertyCompanyRepository propertyCompanyRepository;
    @Autowired private CoordinatorRepository coordinatorRepository;
    @Autowired private PropertyManagerRepository propertyManagerRepository;
    @Autowired private DistributionRecordRepository distributionRecordRepository;
    @Autowired private WalletFlowRepository walletFlowRepository;
    @Autowired private WechatSplitRecordRepository wechatSplitRecordRepository;
    @Autowired private WithdrawalRecordRepository withdrawalRecordRepository;

    @BeforeEach
    void setUp() {
        PropertyCompany company = new PropertyCompany();
        company.setId(PC);
        company.setName("口径唯一测试物业");
        company.setStatus("active");
        propertyCompanyRepository.save(company);

        resident(COO, "13970000001", "口径统筹");
        resident(MGR, "13970000002", "口径经理");

        Coordinator coordinator = new Coordinator();
        coordinator.setId(COO_ID);
        coordinator.setResidentId(COO);
        coordinator.setPropertyCompanyId(PC);
        coordinator.setCommissionRate(new BigDecimal("0.1000"));
        coordinator.setStatus("active");
        coordinatorRepository.save(coordinator);

        // 统筹分账所得 10.00：订单完成即入账，走提现通道（老板口径：并入可提现余额）
        distributionRecordRepository.save(coordinatorShareRecord(new BigDecimal("10.00")));

        PropertyManager manager = new PropertyManager();
        manager.setId(MGR_ID);
        manager.setResidentId(MGR);
        manager.setPropertyCompanyId(PC);
        manager.setCommissionRate(new BigDecimal("0.0500"));
        manager.setStatus("active");
        propertyManagerRepository.save(manager);

        // 经理抽成入账 10.00，其中 4.00 已由微信分账实时到微信（不可再走内部提现）
        walletFlowRepository.save(walletFlow(FLOW_PM, "property_manager", MGR, "10.00", "pm_share"));
        WechatSplitRecord split = new WechatSplitRecord();
        split.setId(SPLIT_PM);
        split.setOrderId(ORDER);
        split.setOwnerType("PROPERTY_MANAGER");
        split.setOwnerId(MGR);
        split.setStatus("finished");
        split.setFreezeAmount(new BigDecimal("4.00"));
        wechatSplitRecordRepository.save(split);

        walletFlowRepository.save(walletFlow(FLOW_RES, "resident", COO, "100.00", "reward_to_wallet"));
    }

    @AfterEach
    void cleanUp() {
        withdrawalRecordRepository.deleteAll(
                withdrawalRecordRepository.findAll().stream()
                        .filter(w -> PC.equals(w.getPropertyCompanyId())).toList());
        wechatSplitRecordRepository.deleteById(SPLIT_PM);
        walletFlowRepository.deleteAllById(List.of(FLOW_PM, FLOW_RES));
        distributionRecordRepository.deleteById(DR);
        propertyManagerRepository.deleteById(MGR_ID);
        coordinatorRepository.deleteById(COO_ID);
        residentRepository.deleteAllById(List.of(MGR, COO));
        propertyCompanyRepository.deleteById(PC);
    }

    // ==================== 分账所得页：补字段 + 与提现校验同源 ====================

    @Test
    @DisplayName("分账所得页：walletIncluded=true，角色项返回可提现/在途/费率/已入账/可提现开关")
    void earningsMine_exposesWalletLinkedFields() throws Exception {
        JsonNode summary = earningsSummaryForCoordinator();

        assertEquals("true", topLevelValue("walletIncluded"),
                "分账所得已并入可提现余额，页面文案要切到「已计入余额」");
        assertEquals("COORDINATOR", summary.path("role").asText());
        assertEquals(0, new BigDecimal("10.00").compareTo(decimal(summary, "payableAmount")));
        assertEquals(0, new BigDecimal("10.00").compareTo(decimal(summary, "withdrawableAmount")),
                "可提现=分账台账入账，与提现校验同一个数");
        assertEquals(0, new BigDecimal("10.00").compareTo(decimal(summary, "walletCreditedAmount")));
        assertEquals(0, BigDecimal.ZERO.compareTo(decimal(summary, "pendingWithdrawalAmount")));
        assertEquals(0, FEE_RATE.compareTo(decimal(summary, "feeRate")));
        assertEquals("true", summary.path("withdrawEnabled").asText());
    }

    @Test
    @DisplayName("页面显示可提现 10.00：刚好提 10.00 成功，多提 0.01 被拒（不多不少，前端不会撞 94003）")
    void earningsWithdrawable_equalsWithdrawalValidationBoundary() throws Exception {
        assertEquals(0, new BigDecimal("10.00")
                        .compareTo(decimal(earningsSummaryForCoordinator(), "withdrawableAmount")),
                "前置：页面口径 10.00");

        mockMvc.perform(post("/coordinators/" + COO_ID + "/withdrawals")
                        .header("Authorization", bearer(COO, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":10.01}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(94003));

        mockMvc.perform(post("/coordinators/" + COO_ID + "/withdrawals")
                        .header("Authorization", bearer(COO, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":10.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feeRate").value(0.0060))
                .andExpect(jsonPath("$.data.feeAmount").value(0.06))
                .andExpect(jsonPath("$.data.actualAmount").value(9.94));

        JsonNode after = earningsSummaryForCoordinator();
        assertEquals(0, BigDecimal.ZERO.compareTo(decimal(after, "withdrawableAmount")),
                "提完后可提现归零");
        assertEquals(0, new BigDecimal("10.00").compareTo(decimal(after, "pendingWithdrawalAmount")),
                "在途提现透出，用户不会以为钱丢了");
    }

    // ==================== 微信分账已到账：展示端与校验端一起扣 ====================

    @Test
    @DisplayName("物业经理：已实时分账 4.00 时，钱包概览可提现=6.00（此前显示 10.00，提 8.00 必被拒）")
    void propertyManagerWallet_displaySubtractsSplitSettled() throws Exception {
        mockMvc.perform(get("/property-managers/my/wallet")
                        .header("Authorization", bearer(MGR, "resident", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalEarning").value(10.00))
                .andExpect(jsonPath("$.data.withdrawableAmount").value(6.00))
                .andExpect(jsonPath("$.data.feeRate").value(0.0060))
                .andExpect(jsonPath("$.data.withdrawFeeBearer").value("payee"));

        mockMvc.perform(post("/property-managers/my/withdrawals")
                        .header("Authorization", bearer(MGR, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":8.00}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/property-managers/my/withdrawals")
                        .header("Authorization", bearer(MGR, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":6.00}"))
                .andExpect(status().isOk());
    }

    // ==================== 余额接口：提交前即可见手续费率 ====================

    @Test
    @DisplayName("住户钱包：withdrawable 与提现校验同源，并返回 feeRate/withdrawFeeBearer")
    void residentWallet_exposesFeeRateBeforeSubmit() throws Exception {
        mockMvc.perform(get("/wallet/my")
                        .header("Authorization", bearer(COO, "resident", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.walletBalance").value(100.00))
                .andExpect(jsonPath("$.data.withdrawableAmount").value(100.00))
                .andExpect(jsonPath("$.data.feeRate").value(0.0060))
                .andExpect(jsonPath("$.data.withdrawFeeBearer").value("payee"));

        mockMvc.perform(post("/wallet/withdrawals")
                        .header("Authorization", bearer(COO, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feeAmount").value(0.60))
                .andExpect(jsonPath("$.data.actualAmount").value(99.40));
    }

    @Test
    @DisplayName("统筹工作台：withdrawable=分账入账−在途，且带费率（与分账所得页同一个数）")
    void coordinatorMyInfo_usesSameWithdrawable() throws Exception {
        mockMvc.perform(get("/coordinators/my")
                        .header("Authorization", bearer(COO, "resident", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalEarnings").value(10.00))
                .andExpect(jsonPath("$.data.withdrawableAmount").value(10.00))
                .andExpect(jsonPath("$.data.feeRate").value(0.0060))
                .andExpect(jsonPath("$.data.withdrawFeeBearer").value("payee"));
    }

    // ==================== 夹具 ====================

    private JsonNode earningsSummaryForCoordinator() throws Exception {
        String body = contentOf(mockMvc.perform(get("/distribution/earnings/mine")
                .header("Authorization", bearer(COO, "resident", PC, null))).andReturn());
        JsonNode summary = objectMapper.readTree(body).path("data").path("summary");
        assertTrue(summary.size() > 0, "应返回 COORDINATOR 汇总项；body=" + body);
        return summary.get(0);
    }

    private String topLevelValue(String field) throws Exception {
        String body = contentOf(mockMvc.perform(get("/distribution/earnings/mine")
                .header("Authorization", bearer(COO, "resident", PC, null))).andReturn());
        return objectMapper.readTree(body).path("data").path(field).asText();
    }

    private DistributionRecord coordinatorShareRecord(BigDecimal share) {
        DistributionRecord record = new DistributionRecord();
        record.setId(DR);
        record.setOrderId(ORDER);
        record.setMerchantId("mch_wsl001");
        record.setPropertyCompanyId(PC);
        record.setResidentId("res_wsl_buyer");
        record.setTotalAmount(new BigDecimal("100.00"));
        record.setCoinCost(BigDecimal.ZERO);
        record.setCommissionRate(new BigDecimal("0.2000"));
        record.setDistributableAmount(new BigDecimal("80.00"));
        record.setPropertyShare(BigDecimal.ZERO);
        record.setCoordinatorId(COO_ID);
        record.setCoordinatorShare(share);
        record.setSectorLeaderShare(BigDecimal.ZERO);
        record.setIndividualLeaderShare(BigDecimal.ZERO);
        record.setBuildingLeaderShare(BigDecimal.ZERO);
        record.setManagerShare(BigDecimal.ZERO);
        record.setPlatformShare(BigDecimal.ZERO);
        return record;
    }

    private WalletFlow walletFlow(String id, String ownerType, String ownerId, String amount, String entryType) {
        WalletFlow flow = new WalletFlow();
        flow.setId(id);
        flow.setOwnerType(ownerType);
        flow.setOwnerId(ownerId);
        flow.setEntryType(entryType);
        flow.setAmount(new BigDecimal(amount));
        flow.setBalanceType("withdrawable");
        flow.setIdempotencyKey(id);
        return flow;
    }

    private Resident resident(String id, String phone, String name) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setPhone(phone);
        resident.setName(name);
        resident.setRole("resident");
        resident.setStatus("active");
        resident.setPropertyCompanyId(PC);
        resident.setCommunityId("com_wsl0001");
        resident.setBuilding("1");
        resident.setUnit("1");
        resident.setRoom("101");
        resident.setPointBalance(0);
        resident.setCoinBalance(BigDecimal.ZERO);
        resident.setTokenVersion(0);
        return residentRepository.save(resident);
    }

    private String contentOf(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private static BigDecimal decimal(JsonNode node, String field) {
        return new BigDecimal(node.path(field).asText());
    }
}
