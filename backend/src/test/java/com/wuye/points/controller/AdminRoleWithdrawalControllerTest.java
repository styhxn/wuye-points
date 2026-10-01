package com.wuye.points.controller;

import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.WithdrawalRecord;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.WithdrawalRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 管理端配送员/多角色提现审批闭环验证：
 * 配送员申请落库 withdrawal_records（withdrawal_type=courier）后，管理端可通过
 * /admin/role-withdrawals 查询并审核。
 */
@DisplayName("管理端-配送员/多角色提现审批")
class AdminRoleWithdrawalControllerTest extends ControllerTestBase {

    @Autowired
    private WithdrawalRecordRepository withdrawalRecordRepository;

    @Autowired
    private PropertyCompanyRepository propertyCompanyRepository;

    @Autowired
    private ResidentRepository residentRepository;

    @BeforeEach
    void seed() {
        PropertyCompany company = new PropertyCompany();
        company.setId("pc_demo001");
        company.setName("演示物业公司");
        company.setStatus("active");
        propertyCompanyRepository.save(company);

        Resident courier = new Resident();
        courier.setId("res_courier01");
        courier.setName("快递员-吴十");
        courier.setPhone("13900000016");
        courier.setPropertyCompanyId("pc_demo001");
        courier.setRole("courier");
        courier.setStatus("active");
        residentRepository.save(courier);

        WithdrawalRecord withdrawal = new WithdrawalRecord();
        withdrawal.setId("wr_test001");
        withdrawal.setWithdrawalType("courier");
        withdrawal.setApplicantId("cm_demo001");
        withdrawal.setResidentId("res_courier01");
        withdrawal.setPropertyCompanyId("pc_demo001");
        withdrawal.setAmount(new BigDecimal("100.00"));
        withdrawal.setFeeRate(new BigDecimal("0.0200"));
        withdrawal.setFeeAmount(new BigDecimal("2.00"));
        withdrawal.setActualAmount(new BigDecimal("98.00"));
        withdrawal.setPlatformFeeShare(new BigDecimal("2.00"));
        withdrawal.setAuditStatus("pending");
        withdrawal.setCreatedAt(LocalDateTime.now());
        withdrawal.setUpdatedAt(LocalDateTime.now());
        withdrawalRecordRepository.save(withdrawal);
    }

    private String adminToken() {
        return bearer("u_platform_admin", "platform_admin", "pc_demo001", null);
    }

    @Test
    @DisplayName("列表: platform_admin 可查到配送员提现记录")
    void listWithdrawals_returnsCourierRecord() throws Exception {
        mockMvc.perform(get("/admin/role-withdrawals")
                        .header("Authorization", adminToken())
                        .param("withdrawalType", "courier"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].id").value("wr_test001"))
                .andExpect(jsonPath("$.data.list[0].withdrawalType").value("courier"))
                .andExpect(jsonPath("$.data.list[0].residentName").value("快递员-吴十"))
                .andExpect(jsonPath("$.data.list[0].auditStatus").value("pending"));
    }

    @Test
    @DisplayName("列表: keyword 按住户姓名/手机号可搜到")
    void listWithdrawals_keywordMatchesResident() throws Exception {
        mockMvc.perform(get("/admin/role-withdrawals")
                        .header("Authorization", adminToken())
                        .param("keyword", "13900000016"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].id").value("wr_test001"));

        mockMvc.perform(get("/admin/role-withdrawals")
                        .header("Authorization", adminToken())
                        .param("keyword", "吴十"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].id").value("wr_test001"));
    }

    @Test
    @DisplayName("汇总: 待审笔数/金额正确")
    void summary_returnsPending() throws Exception {
        mockMvc.perform(get("/admin/role-withdrawals/summary")
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.pendingCount").value(1))
                .andExpect(jsonPath("$.data.pendingAmount").value(100.00));
    }

    @Test
    @DisplayName("审核: approved 成功")
    void audit_approved_succeeds() throws Exception {
        mockMvc.perform(post("/admin/role-withdrawals/wr_test001/audit")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"auditResult\":\"approved\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.auditStatus").value("approved"))
                .andExpect(jsonPath("$.data.auditorId").value("u_platform_admin"));
    }

    @Test
    @DisplayName("审核: rejected 必须填 rejectReason")
    void audit_rejected_requiresReason() throws Exception {
        mockMvc.perform(post("/admin/role-withdrawals/wr_test001/audit")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"auditResult\":\"rejected\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10001));
    }

    @Test
    @DisplayName("审核: approved 后完成打款 completed")
    void audit_approvedThenCompleted() throws Exception {
        mockMvc.perform(post("/admin/role-withdrawals/wr_test001/audit")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"auditResult\":\"approved\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.auditStatus").value("approved"));

        mockMvc.perform(post("/admin/role-withdrawals/wr_test001/audit")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"auditResult\":\"completed\",\"transferProof\":\"https://proof.example.com/x.jpg\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.auditStatus").value("completed"))
                .andExpect(jsonPath("$.data.disburseChannel").value("manual"))
                .andExpect(jsonPath("$.data.transferProof").value("https://proof.example.com/x.jpg"));
    }
}
