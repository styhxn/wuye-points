package com.wuye.points.controller;

import com.wuye.points.entity.ActivityLeaderApplication;
import com.wuye.points.entity.DistributorApplication;
import com.wuye.points.entity.IndividualLeaderApplication;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.ActivityLeaderApplicationRepository;
import com.wuye.points.repository.ActivityLeaderIdentityRepository;
import com.wuye.points.repository.DistributorApplicationRepository;
import com.wuye.points.repository.IndividualLeaderApplicationRepository;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.PlatformMerchantRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 一级代理申请审核、活动组组长申请审核全链路回归。
 *
 * <p>重点覆盖：审核通过时创建 merchant 载体必须显式落库 DB NOT NULL 的
 * merchant_type / is_canteen_main_merchant / serve_all_communities / is_distributor；
 * 以及申请后「我的申请」状态可正确回读、platform_admin 可跨物业查询。</p>
 */
@DisplayName("一级代理/活动组长申请审核全链路")
class DistributorLeaderAuditFlowTest extends ControllerTestBase {

    @Autowired
    private PropertyCompanyRepository propertyCompanyRepository;
    @Autowired
    private ResidentRepository residentRepository;
    @Autowired
    private DistributorApplicationRepository distributorApplicationRepository;
    @Autowired
    private IndividualLeaderApplicationRepository individualLeaderApplicationRepository;
    @Autowired
    private ActivityLeaderApplicationRepository activityLeaderApplicationRepository;
    @Autowired
    private ActivityLeaderIdentityRepository activityLeaderIdentityRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    @Autowired
    private PlatformMerchantRepository platformMerchantRepository;

    @BeforeEach
    void seed() {
        merchantPropertyConfigRepository.deleteAll();
        merchantRepository.deleteAll();
        platformMerchantRepository.deleteAll();
        activityLeaderIdentityRepository.deleteAll();
        activityLeaderApplicationRepository.deleteAll();
        individualLeaderApplicationRepository.deleteAll();
        distributorApplicationRepository.deleteAll();
        residentRepository.deleteAll();
        propertyCompanyRepository.deleteAll();

        saveCompany("pc_test001", "测试物业一");
        saveCompany("pc_test002", "测试物业二");
        saveResident("res_applicant", "resident", "pc_test001", "张阿姨", "13624964818");
        saveResident("res_applicant2", "resident", "pc_test002", "李阿姨", "13624964819");
        saveResident("res_admin", "property_admin", "pc_test001", "物业管理员", "13624964810");
        saveResident("res_platform", "platform_admin", "pc_test001", "平台管理员", "13624964811");
    }

    @Test
    @DisplayName("一级代理审核通过：自动建商家载体且 NOT NULL 列全部有值")
    void distributorAuditApproved_shouldCreateMerchantWithRequiredDefaults() throws Exception {
        distributorApplicationRepository.save(distributorApplication(
                "dap_test001", "res_applicant", "pc_test001", "pending", LocalDateTime.of(2026, 10, 20, 10, 0)));

        mockMvc.perform(post("/admin/distributor-applications/dap_test001/audit")
                        .header("Authorization", bearer("res_admin", "property_admin", "pc_test001", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"auditStatus\":\"approved\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.auditStatus").value("approved"))
                .andExpect(jsonPath("$.data.distributorMerchantId").isNotEmpty());

        Merchant merchant = merchantRepository.findByResidentId("res_applicant").get(0);
        assertTrue(merchant.getIsDistributor());
        assertEquals("approved", merchant.getAuditStatus());
        assertEquals("distributor", merchant.getMerchantSource());
        assertEquals("goods", merchant.getMerchantType());
        assertFalse(merchant.getIsCanteenMainMerchant());
        assertTrue(merchant.getServeAllCommunities());
        assertTrue(merchantPropertyConfigRepository
                .findByMerchantIdAndPropertyCompanyId(merchant.getId(), "pc_test001").isPresent());
        assertEquals(merchant.getId(), distributorApplicationRepository.findById("dap_test001")
                .orElseThrow().getDistributorMerchantId());
    }

    @Test
    @DisplayName("组长审核通过：建身份 + 自动开组长小店，role 保持 resident")
    void activityLeaderAuditApproved_shouldCreateIdentityAndShop() throws Exception {
        activityLeaderApplicationRepository.save(activityLeaderApplication(
                "ala_test001", "res_applicant", "pc_test001", "pending", LocalDateTime.of(2026, 10, 20, 10, 0)));

        mockMvc.perform(post("/admin/activity-leader-applications/ala_test001/audit")
                        .header("Authorization", bearer("res_admin", "property_admin", "pc_test001", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"auditStatus\":\"approved\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.auditStatus").value("approved"))
                .andExpect(jsonPath("$.data.activityLeaderIdentityId").isNotEmpty());

        assertTrue(activityLeaderIdentityRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                        "res_applicant", "pc_test001", "active").isPresent());

        Merchant merchant = merchantRepository.findByResidentId("res_applicant").get(0);
        assertEquals("group_leader", merchant.getMerchantSource());
        assertEquals("group_leader", merchant.getMerchantType());
        assertFalse(merchant.getIsCanteenMainMerchant());
        assertFalse(merchant.getIsDistributor());
        assertTrue(merchant.getServeAllCommunities());
        assertTrue(merchantPropertyConfigRepository
                .findByMerchantIdAndPropertyCompanyId(merchant.getId(), "pc_test001").isPresent());

        assertEquals("resident", residentRepository.findById("res_applicant").orElseThrow().getRole());

        mockMvc.perform(get("/activity-leader-applications/my-application")
                        .header("Authorization", bearer("res_applicant", "resident", "pc_test001", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("approved"));
    }

    @Test
    @DisplayName("组长审核驳回：记录原因、不建身份与店铺，C 端可回读驳回原因")
    void activityLeaderAuditRejected_shouldNotCreateIdentityOrShop() throws Exception {
        activityLeaderApplicationRepository.save(activityLeaderApplication(
                "ala_test002", "res_applicant", "pc_test001", "pending", LocalDateTime.of(2026, 10, 20, 10, 0)));

        mockMvc.perform(post("/admin/activity-leader-applications/ala_test002/audit")
                        .header("Authorization", bearer("res_admin", "property_admin", "pc_test001", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"auditStatus\":\"rejected\",\"rejectReason\":\"活动经验不足\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.auditStatus").value("rejected"))
                .andExpect(jsonPath("$.data.rejectReason").value("活动经验不足"));

        assertTrue(activityLeaderIdentityRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                        "res_applicant", "pc_test001", "active").isEmpty());
        assertTrue(merchantRepository.findByResidentId("res_applicant").isEmpty());

        mockMvc.perform(get("/activity-leader-applications/my-application")
                        .header("Authorization", bearer("res_applicant", "resident", "pc_test001", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("rejected"))
                .andExpect(jsonPath("$.data.rejectReason").value("活动经验不足"));
    }

    @Test
    @DisplayName("申请状态回读：一级代理/个体负责人返回最新一条而非空")
    void myApplication_shouldReturnLatestApplication() throws Exception {
        distributorApplicationRepository.save(distributorApplication(
                "dap_old", "res_applicant", "pc_test001", "rejected", LocalDateTime.of(2026, 10, 1, 10, 0)));
        distributorApplicationRepository.save(distributorApplication(
                "dap_new", "res_applicant", "pc_test001", "pending", LocalDateTime.of(2026, 10, 2, 10, 0)));

        mockMvc.perform(get("/distributors/my/application")
                        .header("Authorization", bearer("res_applicant", "resident", "pc_test001", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.applicationId").value("dap_new"))
                .andExpect(jsonPath("$.data.status").value("pending"));

        individualLeaderApplicationRepository.save(individualLeaderApplication(
                "ila_old", "rejected", LocalDateTime.of(2026, 10, 1, 10, 0)));
        individualLeaderApplicationRepository.save(individualLeaderApplication(
                "ila_new", "pending", LocalDateTime.of(2026, 10, 2, 10, 0)));

        mockMvc.perform(get("/individual-leaders/my-application")
                        .header("Authorization", bearer("res_applicant", "resident", "pc_test001", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.applicationId").value("ila_new"))
                .andExpect(jsonPath("$.data.status").value("pending"));
    }

    @Test
    @DisplayName("platform_admin 不传 propertyCompanyId 时跨物业返回全部申请")
    void platformAdminListWithoutProperty_shouldReturnAllProperties() throws Exception {
        distributorApplicationRepository.save(distributorApplication(
                "dap_p1", "res_applicant", "pc_test001", "pending", LocalDateTime.of(2026, 10, 20, 10, 0)));
        distributorApplicationRepository.save(distributorApplication(
                "dap_p2", "res_applicant2", "pc_test002", "pending", LocalDateTime.of(2026, 10, 20, 10, 1)));

        mockMvc.perform(get("/admin/distributor-applications")
                        .header("Authorization", bearer("res_platform", "platform_admin", "pc_test001", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pagination.total").value(2));

        activityLeaderApplicationRepository.save(activityLeaderApplication(
                "ala_p1", "res_applicant", "pc_test001", "pending", LocalDateTime.of(2026, 10, 20, 10, 0)));
        activityLeaderApplicationRepository.save(activityLeaderApplication(
                "ala_p2", "res_applicant2", "pc_test002", "pending", LocalDateTime.of(2026, 10, 20, 10, 1)));

        mockMvc.perform(get("/admin/activity-leader-applications")
                        .header("Authorization", bearer("res_platform", "platform_admin", "pc_test001", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pagination.total").value(2));
    }

    private DistributorApplication distributorApplication(
            String id, String residentId, String propertyCompanyId, String auditStatus, LocalDateTime createdAt) {
        DistributorApplication application = new DistributorApplication();
        application.setId(id);
        application.setResidentId(residentId);
        application.setPropertyCompanyId(propertyCompanyId);
        application.setContactName("张阿姨");
        application.setContactPhone("13624964818");
        application.setBusinessScope("fresh");
        application.setServiceScope("community");
        application.setAuditStatus(auditStatus);
        application.setCreatedAt(createdAt);
        application.setUpdatedAt(createdAt);
        return application;
    }

    private ActivityLeaderApplication activityLeaderApplication(
            String id, String residentId, String propertyCompanyId, String auditStatus, LocalDateTime createdAt) {
        ActivityLeaderApplication application = new ActivityLeaderApplication();
        application.setId(id);
        application.setResidentId(residentId);
        application.setPropertyCompanyId(propertyCompanyId);
        application.setActivityField("文艺");
        application.setRemark("组织过社区活动");
        application.setAuditStatus(auditStatus);
        application.setCreatedAt(createdAt);
        application.setUpdatedAt(createdAt);
        return application;
    }

    private IndividualLeaderApplication individualLeaderApplication(
            String id, String auditStatus, LocalDateTime createdAt) {
        IndividualLeaderApplication application = new IndividualLeaderApplication();
        application.setId(id);
        application.setResidentId("res_applicant");
        application.setPropertyCompanyId("pc_test001");
        application.setSector("文娱板块");
        application.setAuditStatus(auditStatus);
        application.setCreatedAt(createdAt);
        application.setUpdatedAt(createdAt);
        return application;
    }

    private void saveCompany(String id, String name) {
        PropertyCompany company = new PropertyCompany();
        company.setId(id);
        company.setName(name);
        company.setStatus("active");
        propertyCompanyRepository.save(company);
    }

    private void saveResident(String id, String role, String propertyCompanyId, String name, String phone) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setRole(role);
        resident.setStatus("active");
        resident.setPropertyCompanyId(propertyCompanyId);
        resident.setName(name);
        resident.setPhone(phone);
        resident.setUserType("owner");
        residentRepository.save(resident);
    }
}
