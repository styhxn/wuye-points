package com.wuye.points.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 住户钱包闭环：奖励金转钱包 → 查询余额 → 提现申请 → 管理端审核完成 → 余额冲减。
 */
@DisplayName("住户钱包-转入/查询/提现闭环")
class ResidentWalletWithdrawalTest extends ControllerTestBase {

    private String propertyCompanyId;
    private String residentId;

    @Autowired
    private PropertyCompanyRepository propertyCompanyRepository;
    @Autowired
    private ResidentRepository residentRepository;

    @BeforeEach
    void seed() {
        // 每次用例使用唯一主体，避免共享 H2 中上一用例的钱包流水污染余额
        propertyCompanyId = "pc_wallet_" + System.nanoTime();
        residentId = "res_wallet_" + System.nanoTime();

        PropertyCompany company = new PropertyCompany();
        company.setId(propertyCompanyId);
        company.setName("钱包测试物业");
        company.setStatus("active");
        propertyCompanyRepository.save(company);

        Resident resident = new Resident();
        resident.setId(residentId);
        resident.setName("钱包住户");
        resident.setPhone("13900000099");
        resident.setPropertyCompanyId(propertyCompanyId);
        resident.setRole("resident");
        resident.setUserType("owner");
        resident.setStatus("active");
        resident.setCoinBalance(new BigDecimal("100.00"));
        resident.setWithdrawalBlocked(false);
        residentRepository.save(resident);
    }

    private String residentToken() {
        return bearer(residentId, "resident", propertyCompanyId, null);
    }

    private String adminToken() {
        return bearer("u_wallet_admin", "platform_admin", propertyCompanyId, null);
    }

    @Test
    @DisplayName("转入钱包后可查询、提现，审核完成后钱包余额扣减")
    void transferQueryWithdrawAndComplete() throws Exception {
        // 1. 个人奖励金 100 转钱包
        mockMvc.perform(post("/property-coins/transfer-to-wallet")
                        .header("Authorization", residentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"coinAmount\":100.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.coinBalance").value(0.00))
                .andExpect(jsonPath("$.data.walletBalance").value(100.00));

        // 2. 钱包余额可见
        mockMvc.perform(get("/wallet/my")
                        .header("Authorization", residentToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.coinBalance").value(0.00))
                .andExpect(jsonPath("$.data.walletBalance").value(100.00))
                .andExpect(jsonPath("$.data.occupiedWithdrawalAmount").value(0))
                .andExpect(jsonPath("$.data.withdrawableAmount").value(100.00));

        // 3. 申请提现 40
        MvcResult applyResult = mockMvc.perform(post("/wallet/withdrawals")
                        .header("Authorization", residentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":40.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.withdrawalType").value("resident_wallet"))
                .andExpect(jsonPath("$.data.status").value("pending"))
                .andReturn();
        JsonNode applyJson = objectMapper.readTree(applyResult.getResponse().getContentAsString());
        String withdrawalId = applyJson.path("data").path("id").asText();

        // 4. 在途占用 40，可提现 60
        mockMvc.perform(get("/wallet/my")
                        .header("Authorization", residentToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.walletBalance").value(100.00))
                .andExpect(jsonPath("$.data.occupiedWithdrawalAmount").value(40.00))
                .andExpect(jsonPath("$.data.withdrawableAmount").value(60.00));

        // 5. 管理端审核通过
        mockMvc.perform(post("/admin/role-withdrawals/" + withdrawalId + "/audit")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"auditResult\":\"approved\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.auditStatus").value("approved"));

        // 6. 审核完成：写入负向钱包流水，累计余额 60，占用归零
        mockMvc.perform(post("/admin/role-withdrawals/" + withdrawalId + "/audit")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"auditResult\":\"completed\",\"transferProof\":\"https://proof.example.com/1.jpg\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.auditStatus").value("completed"));

        mockMvc.perform(get("/wallet/my")
                        .header("Authorization", residentToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.walletBalance").value(60.00))
                .andExpect(jsonPath("$.data.occupiedWithdrawalAmount").value(0))
                .andExpect(jsonPath("$.data.withdrawableAmount").value(60.00));

        // 7. 提现记录可查
        mockMvc.perform(get("/wallet/withdrawals")
                        .header("Authorization", residentToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].id").value(withdrawalId))
                .andExpect(jsonPath("$.data.list[0].status").value("completed"));
    }

    @Test
    @DisplayName("超过可提现余额返回 94006")
    void withdrawOverBalanceRejected() throws Exception {
        mockMvc.perform(post("/property-coins/transfer-to-wallet")
                        .header("Authorization", residentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"coinAmount\":50.00}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/wallet/withdrawals")
                        .header("Authorization", residentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":80.00}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(94006));
    }
}
