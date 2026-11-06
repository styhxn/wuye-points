package com.wuye.points.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.wuye.points.entity.ActivityGroup;
import com.wuye.points.entity.Coordinator;
import com.wuye.points.entity.DirectedMessageTask;
import com.wuye.points.entity.IndividualLeader;
import com.wuye.points.entity.Permission;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.SectorLeader;
import com.wuye.points.entity.UserPermission;
import com.wuye.points.repository.ActivityGroupRepository;
import com.wuye.points.repository.CoordinatorRepository;
import com.wuye.points.repository.DirectedMessageTaskRepository;
import com.wuye.points.repository.IndividualLeaderRepository;
import com.wuye.points.repository.PermissionRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.SectorLeaderRepository;
import com.wuye.points.repository.UserPermissionRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 多身份（统筹 + 板块负责人 + 个体负责人 + 楼长）账号的管理端写权限回归。
 *
 * <p>线上 2026-11-06 前端反馈：统筹在工作台「编辑活动组」保存、「发定向通告」都被打成
 * 「权限不足」。两条根因不同：</p>
 * <ul>
 *   <li>活动组：Service 先按 {@code isLeaderRole()}（含 role=resident）把统筹当组长本人，
 *       非本组组长直接拒，权限码 activity_group.manage 根本没参与判断 → 后端已修。</li>
 *   <li>定向通告：走权限码 directed_message.send，统筹预设里没有该码 → 属权限配置，
 *       授予后必须可用（本类把「未授予 403 / 授予后 200」两端都钉住，防止再退化成代码挡人）。</li>
 * </ul>
 */
@DisplayName("多身份账号：统筹写权限")
class CoordinatorMultiIdentityPermissionTest extends ControllerTestBase {

    private static final String PC = "pc_coo0001";
    private static final String COO = "res_coo_lead";
    private static final String GROUP_OWNER = "res_coo_owner";
    private static final String TARGET = "res_coo_target";
    private static final String GROUP_ID = "act_coo0001";
    private static final String PERM_ACTIVITY_MANAGE = "perm_coo_activity_manage";
    private static final String PERM_DIRECTED_SEND = "perm_coo_directed_send";

    @Autowired private ResidentRepository residentRepository;
    @Autowired private PropertyCompanyRepository propertyCompanyRepository;
    @Autowired private CoordinatorRepository coordinatorRepository;
    @Autowired private SectorLeaderRepository sectorLeaderRepository;
    @Autowired private IndividualLeaderRepository individualLeaderRepository;
    @Autowired private ActivityGroupRepository activityGroupRepository;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private UserPermissionRepository userPermissionRepository;
    @Autowired private DirectedMessageTaskRepository directedMessageTaskRepository;
    @Autowired private com.wuye.points.repository.CommunityServiceRepository communityServiceRepository;

    /** 服务发布用例新建的行（ID 由雪花生成，只能记下来在 cleanUp 里删）。 */
    private String createdServiceId;

    @BeforeEach
    void setUp() {
        PropertyCompany company = new PropertyCompany();
        company.setId(PC);
        company.setName("多身份测试物业");
        company.setStatus("active");
        propertyCompanyRepository.save(company);

        // 与线上 res_35217712f21000 同型：库里 role 已归一为 resident，身份全靠标签表
        resident(COO, "13960000001", "统筹测试账号");
        resident(GROUP_OWNER, "13960000002", "组长测试账号");
        resident(TARGET, "13960000003", "定向消息对象");

        coordinatorRepository.save(coordinator("coo_coo0001", COO));
        sectorLeaderRepository.save(sector("sl_coo0001", COO));
        individualLeaderRepository.save(individual("il_coo0001", COO));

        permissionRepository.save(permission(PERM_ACTIVITY_MANAGE, "activity_group.manage", "活动组管理"));
        permissionRepository.save(permission(PERM_DIRECTED_SEND, "directed_message.send", "定向消息发送"));

        activityGroupRepository.save(group());
    }

    @AfterEach
    void cleanUp() {
        directedMessageTaskRepository.deleteAll(directedMessageTaskRepository.findAll().stream()
                .filter(task -> PC.equals(task.getPropertyCompanyId())).toList());
        if (createdServiceId != null) {
            communityServiceRepository.deleteById(createdServiceId);
            createdServiceId = null;
        }
        userPermissionRepository.deleteAllById(List.of("up_coo_activity", "up_coo_directed"));
        permissionRepository.deleteAllById(List.of(PERM_ACTIVITY_MANAGE, PERM_DIRECTED_SEND));
        activityGroupRepository.deleteById(GROUP_ID);
        individualLeaderRepository.deleteById("il_coo0001");
        sectorLeaderRepository.deleteById("sl_coo0001");
        coordinatorRepository.deleteById("coo_coo0001");
        residentRepository.deleteAllById(List.of(TARGET, GROUP_OWNER, COO));
        propertyCompanyRepository.deleteById(PC);
    }

    private Resident resident(String id, String phone, String name) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setPhone(phone);
        resident.setName(name);
        resident.setRole("resident");
        resident.setStatus("active");
        resident.setPropertyCompanyId(PC);
        resident.setCommunityId("com_coo0001");
        resident.setBuilding("1");
        resident.setUnit("1");
        resident.setRoom("101");
        resident.setPointBalance(0);
        resident.setCoinBalance(BigDecimal.ZERO);
        resident.setTokenVersion(0);
        return residentRepository.save(resident);
    }

    private Coordinator coordinator(String id, String residentId) {
        Coordinator coordinator = new Coordinator();
        coordinator.setId(id);
        coordinator.setResidentId(residentId);
        coordinator.setPropertyCompanyId(PC);
        coordinator.setCommissionRate(new BigDecimal("0.1000"));
        coordinator.setStatus("active");
        return coordinator;
    }

    private SectorLeader sector(String id, String residentId) {
        SectorLeader leader = new SectorLeader();
        leader.setId(id);
        leader.setResidentId(residentId);
        leader.setPropertyCompanyId(PC);
        leader.setSector("cleaning");
        leader.setStatus("active");
        return leader;
    }

    private IndividualLeader individual(String id, String residentId) {
        IndividualLeader leader = new IndividualLeader();
        leader.setId(id);
        leader.setResidentId(residentId);
        leader.setPropertyCompanyId(PC);
        leader.setSectorLeaderId("sl_coo0001");
        leader.setSector("cleaning");
        leader.setName("个体负责人测试");
        leader.setCommissionRate(new BigDecimal("0.0100"));
        leader.setStatus("active");
        return leader;
    }

    private Permission permission(String id, String code, String name) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setCode(code);
        permission.setName(name);
        permission.setModule("activity");
        permission.setStatus("active");
        return permission;
    }

    private void grant(String userPermissionId, String permissionId, boolean granted) {
        UserPermission override = new UserPermission();
        override.setId(userPermissionId);
        override.setResidentId(COO);
        override.setPermissionId(permissionId);
        override.setGranted(granted);
        override.setPropertyCompanyId(PC);
        override.setOperatorId(GROUP_OWNER);
        userPermissionRepository.save(override);
    }

    private ActivityGroup group() {
        ActivityGroup group = new ActivityGroup();
        group.setId(GROUP_ID);
        group.setName("统筹可代管的组");
        group.setLeaderId(GROUP_OWNER);
        group.setPropertyCompanyId(PC);
        group.setMonthlyFee(new BigDecimal("10.00"));
        group.setYearlyFee(new BigDecimal("100.00"));
        group.setReviewStatus("approved");
        group.setStatus("active");
        return group;
    }

    private String contentOf(org.springframework.test.web.servlet.MvcResult result)
            throws java.io.UnsupportedEncodingException {
        return result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
    }

    // ==================== 活动组：统筹按权限码代管本物业的组 ====================

    @Test
    @DisplayName("统筹持有 activity_group.manage：编辑别人当组长的本物业活动组 → 200（此前恒 403）")
    void coordinatorWithPermission_canUpdateOtherLeadersGroup() throws Exception {
        grant("up_coo_activity", PERM_ACTIVITY_MANAGE, true);

        mockMvc.perform(put("/activity-groups/" + GROUP_ID)
                        .header("Authorization", bearer(COO, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"统筹改名成功\",\"description\":\"改过了\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("统筹改名成功"));
    }

    @Test
    @DisplayName("统筹未持有该权限码：编辑活动组仍 403（放开的是权限码，不是身份）")
    void coordinatorWithoutPermission_stillForbidden() throws Exception {
        mockMvc.perform(put("/activity-groups/" + GROUP_ID)
                        .header("Authorization", bearer(COO, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"不该改成这样\"}"))
                .andExpect(status().isForbidden());

        assertEquals("统筹可代管的组",
                activityGroupRepository.findById(GROUP_ID).orElseThrow().getName());
    }

    @Test
    @DisplayName("统筹的用户级授权被显式关掉（granted=false）：预设再全也照样 403")
    void coordinatorWithExplicitDeny_isForbidden() throws Exception {
        grant("up_coo_activity", PERM_ACTIVITY_MANAGE, false);

        mockMvc.perform(put("/activity-groups/" + GROUP_ID)
                        .header("Authorization", bearer(COO, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"不该改成这样\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("纯住户（无统筹标签）改别人的组 → 403，本次放开没有波及组长口径")
    void plainResident_cannotUpdateOthersGroup() throws Exception {
        mockMvc.perform(put("/activity-groups/" + GROUP_ID)
                        .header("Authorization", bearer(TARGET, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"住户改名\"}"))
                .andExpect(status().isForbidden());
    }

    // ==================== 定向通告：权限码授予后即可用 ====================

    private String directedMessagePayload() {
        return "{\"title\":\"统筹定向通告\",\"content\":\"测试内容\","
                + "\"officialSenderType\":\"coordinator\",\"propertyCompanyId\":\"" + PC + "\"}";
    }

    @Test
    @DisplayName("统筹未授予 directed_message.send：发定向通告 403（线上表现）")
    void coordinatorWithoutSendPermission_isForbidden() throws Exception {
        mockMvc.perform(post("/admin/directed-messages")
                        .header("Authorization", bearer(COO, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(directedMessagePayload()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("授予 directed_message.send 后：统筹即可发定向通告 → 200（配置项，不是代码挡人）")
    void coordinatorWithSendPermission_canSend() throws Exception {
        grant("up_coo_directed", PERM_DIRECTED_SEND, true);
        long before = directedMessageTaskRepository
                .countByPropertyCompanyIdAndCreatedAtGreaterThanEqual(PC,
                        java.time.LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/admin/directed-messages")
                        .header("Authorization", bearer(COO, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(directedMessagePayload()))
                .andExpect(status().isOk());

        assertEquals(before + 1, directedMessageTaskRepository
                        .countByPropertyCompanyIdAndCreatedAtGreaterThanEqual(PC,
                                java.time.LocalDateTime.now().minusDays(1)),
                "授予后应真实落一条定向通告任务");
    }

    // ==================== 服务提供者类型：多身份不被统筹遮蔽 ====================

    @Test
    @DisplayName("统筹同时是个体负责人：发布服务的提供者类型是 individual_leader，不再是「居民」")
    void coordinatorAlsoIndividualLeader_publishesAsIndividualLeader() throws Exception {
        String body = contentOf(mockMvc.perform(post("/services")
                        .header("Authorization", bearer(COO, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"多身份测试服务\",\"category\":\"cleaning_repair\","
                                + "\"price\":1.00,\"priceUnit\":\"次\"}"))
                .andExpect(status().isOk())
                .andReturn());

        JsonNode data = objectMapper.readTree(body).path("data");
        createdServiceId = data.path("id").asText();
        assertEquals("individual_leader", data.path("providerType").asText(),
                "提供者类型应取个体负责人，而不是被统筹身份遮蔽成 resident；响应=" + body);
    }
}
