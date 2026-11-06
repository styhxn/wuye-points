package com.wuye.points.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.Family;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.BuildingChangeRequestRepository;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.FamilyRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 家庭与房号冲突整改回归（2026-11-06 老板口径：乙由甲添加为家庭成员，而不是自己申请入住）。
 *
 * <p>改造前三条链路各自为政：可选房号只由前端选择器过滤、{@code POST /families} 与楼栋变更审批
 * 都不查占用（能造出两个家庭同房号）；添加家庭成员只搬余额不写地址（成员永远是「无房号住户」）；
 * 家庭地址变更只改 {@code families}，成员行留旧房号（前端表现为「不是同一个房号」）。</p>
 */
@DisplayName("家庭与房号：占用校验与地址同步")
class FamilyRoomOccupancyTest extends ControllerTestBase {

    private static final String PC = "pc_fam0001";
    private static final String COM = "com_fam0001";
    private static final String OWNER = "res_fam_owner";
    private static final String SPOUSE = "res_fam_spouse";
    private static final String STRANGER = "res_fam_stranger";
    private static final String ADMIN = "res_fam_admin";

    @Autowired private PropertyCompanyRepository propertyCompanyRepository;
    @Autowired private CommunityRepository communityRepository;
    @Autowired private ResidentRepository residentRepository;
    @Autowired private FamilyRepository familyRepository;
    @Autowired private BuildingChangeRequestRepository buildingChangeRequestRepository;

    @BeforeEach
    void setUp() {
        PropertyCompany company = new PropertyCompany();
        company.setId(PC);
        company.setName("家庭房号测试物业");
        company.setStatus("active");
        propertyCompanyRepository.save(company);

        Community community = new Community();
        community.setId(COM);
        community.setPropertyCompanyId(PC);
        community.setName("家庭房号测试小区");
        community.setStatus("active");
        communityRepository.save(community);

        resident(OWNER, "13950000001", "resident", null);
        resident(SPOUSE, "13950000002", "resident", null);
        resident(STRANGER, "13950000003", "resident", null);
        resident(ADMIN, "13950000004", "property_admin", null);
    }

    @AfterEach
    void cleanUp() {
        for (String residentId : List.of(OWNER, SPOUSE, STRANGER)) {
            buildingChangeRequestRepository.deleteAll(
                    buildingChangeRequestRepository.findByResidentIdOrderByAppliedAtDesc(residentId));
        }
        familyRepository.deleteAll(familyRepository.findByPropertyCompanyId(PC));
        residentRepository.deleteAllById(List.of(OWNER, SPOUSE, STRANGER, ADMIN));
        communityRepository.deleteById(COM);
        propertyCompanyRepository.deleteById(PC);
    }

    private Resident resident(String id, String phone, String role, String familyId) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setPhone(phone);
        resident.setName("家庭测试" + id.substring(4));
        resident.setRole(role);
        resident.setStatus("active");
        resident.setPropertyCompanyId(PC);
        resident.setCommunityId(COM);
        resident.setPointBalance(0);
        resident.setCoinBalance(BigDecimal.ZERO);
        resident.setTokenVersion(0);
        resident.setFamilyId(familyId);
        return residentRepository.save(resident);
    }

    private String createFamilyBody(String building, String unit, String room) {
        return "{\"name\":\"测试家庭\",\"communityId\":\"" + COM + "\","
                + "\"building\":\"" + building + "\",\"unit\":\"" + unit + "\",\"room\":\"" + room + "\"}";
    }

    private String contentOf(org.springframework.test.web.servlet.MvcResult result)
            throws java.io.UnsupportedEncodingException {
        return result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
    }

    private JsonNode json(String body) throws Exception {
        return objectMapper.readTree(body);
    }

    private String createFamilyAs(String residentId, String building, String unit, String room) throws Exception {
        return contentOf(mockMvc.perform(post("/families")
                        .header("Authorization", bearer(residentId, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createFamilyBody(building, unit, room)))
                .andExpect(status().isOk())
                .andReturn());
    }

    private Resident reload(String id) {
        return residentRepository.findById(id).orElseThrow();
    }

    // ==================== ① 房号占用改由后端判定 ====================

    @Test
    @DisplayName("户主建家庭后，他人再对同一房号建家庭 → 400，并引导走「添加家庭成员」")
    void secondFamilyOnSameRoom_isRejectedWithGuidance() throws Exception {
        createFamilyAs(OWNER, "4", "1", "401");

        String body = contentOf(mockMvc.perform(post("/families")
                        .header("Authorization", bearer(SPOUSE, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createFamilyBody("4", "1", "401")))
                .andExpect(status().isBadRequest())
                .andReturn());

        assertTrue(json(body).path("message").asText().contains("家庭成员"),
                "报错要告诉用户该走哪条路，实际=" + body);
        assertEquals(1, familyRepository.findByPropertyCompanyId(PC).size(), "不应落第二个家庭");
    }

    @Test
    @DisplayName("楼栋变更申请提交到已被占房号 → 400（不等审批才发现冲突）")
    void buildingChangeToOccupiedRoom_rejectedAtSubmit() throws Exception {
        createFamilyAs(STRANGER, "5", "2", "502");

        mockMvc.perform(post("/residents/me/building-changes")
                        .header("Authorization", bearer(OWNER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"communityId\":\"" + COM + "\",\"building\":\"5\","
                                + "\"unit\":\"2\",\"floor\":\"5\",\"room\":\"502\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("同家庭成员申请自家房号：占用判定放行（成员改地址另有口径，见下方 403 用例）")
    void sameFamilyMember_roomItselfIsNotOccupied() throws Exception {
        String created = createFamilyAs(OWNER, "4", "1", "401");
        String familyId = json(created).path("data").path("id").asText();
        mockMvc.perform(post("/families/" + familyId + "/members")
                        .header("Authorization", bearer(OWNER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13950000002\",\"relation\":\"spouse\"}"))
                .andExpect(status().isOk());

        // 配偶已被占用判定放行（本人+本家庭不算占用），但成员自助改楼栋被单独禁止：
        // 断言点在 familyMember_cannotSubmitOwnBuildingChange，这里只验证不是被「房号占用」挡的。
        String body = contentOf(mockMvc.perform(post("/residents/me/building-changes")
                        .header("Authorization", bearer(SPOUSE, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"communityId\":\"" + COM + "\",\"building\":\"4\","
                                + "\"unit\":\"1\",\"floor\":\"4\",\"room\":\"401\"}"))
                .andReturn());
        assertTrue(json(body).path("message").asText().contains("户主"),
                "应命中「由户主申请」而非「房号已被占用」，实际=" + body);
    }

    @Test
    @DisplayName("同一房号两个都还没建家庭的住户：先建者成户主，后建者被引导添加，不会互相挡死")
    void twoUnaffiliatedResidents_noDeadLock() throws Exception {
        setAddress(OWNER, "4", "1", "401");
        setAddress(SPOUSE, "4", "1", "401");

        createFamilyAs(OWNER, "4", "1", "401");

        String body = contentOf(mockMvc.perform(post("/families")
                        .header("Authorization", bearer(SPOUSE, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createFamilyBody("4", "1", "401")))
                .andExpect(status().isBadRequest())
                .andReturn());
        assertTrue(json(body).path("message").asText().contains("家庭成员"), "实际=" + body);
    }

    @Test
    @DisplayName("房号由租住人员住着时，业主建家庭被挡（租户建不了家庭，families 表表达不了这种占用）")
    void tenantOccupiedRoom_blocksFamilyCreation() throws Exception {
        Resident tenant = reload(STRANGER);
        tenant.setUserType("tenant");
        residentRepository.save(tenant);
        setAddress(STRANGER, "5", "2", "502");

        String body = contentOf(mockMvc.perform(post("/families")
                        .header("Authorization", bearer(OWNER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createFamilyBody("5", "2", "502")))
                .andExpect(status().isBadRequest())
                .andReturn());
        assertTrue(json(body).path("message").asText().contains("租住"), "实际=" + body);
    }

    private void setAddress(String residentId, String building, String unit, String room) {
        Resident resident = reload(residentId);
        resident.setBuilding(building);
        resident.setUnit(unit);
        resident.setRoom(room);
        residentRepository.save(resident);
    }

    @Test
    @DisplayName("成员被移出家庭后不再占房号：楼栋/单元/房号清空，小区保留")
    void removeMember_releasesRoomButKeepsCommunity() throws Exception {
        String created = createFamilyAs(OWNER, "4", "1", "401");
        String familyId = json(created).path("data").path("id").asText();
        mockMvc.perform(post("/families/" + familyId + "/members")
                        .header("Authorization", bearer(OWNER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13950000002\",\"relation\":\"spouse\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/families/" + familyId + "/members/" + SPOUSE)
                        .param("reason", "离异分户")
                        .header("Authorization", bearer(OWNER, "resident", PC, null)))
                .andExpect(status().isOk());

        Resident spouse = reload(SPOUSE);
        org.junit.jupiter.api.Assertions.assertNull(spouse.getFamilyId());
        org.junit.jupiter.api.Assertions.assertNull(spouse.getRoom(), "退出家庭后不得继续占住房号");
        org.junit.jupiter.api.Assertions.assertNull(spouse.getBuilding());
        assertEquals(COM, spouse.getCommunityId(), "仍属该小区账号，与未填地址的新注册用户同状态");
    }

    @Test
    @DisplayName("非户主成员自己申请改楼栋 → 403，提示由户主申请或先移出家庭")
    void familyMember_cannotSubmitOwnBuildingChange() throws Exception {
        String created = createFamilyAs(OWNER, "4", "1", "401");
        String familyId = json(created).path("data").path("id").asText();
        mockMvc.perform(post("/families/" + familyId + "/members")
                        .header("Authorization", bearer(OWNER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13950000002\",\"relation\":\"spouse\"}"))
                .andExpect(status().isOk());

        String body = contentOf(mockMvc.perform(post("/residents/me/building-changes")
                        .header("Authorization", bearer(SPOUSE, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"communityId\":\"" + COM + "\",\"building\":\"8\","
                                + "\"unit\":\"1\",\"floor\":\"8\",\"room\":\"801\"}"))
                .andExpect(status().isForbidden())
                .andReturn());
        assertTrue(json(body).path("message").asText().contains("户主"), "实际=" + body);
        assertEquals("401", reload(SPOUSE).getRoom());
    }

    @Test
    @DisplayName("户主自己申请改楼栋不受影响（审批通过会同步全家）")
    void familyOwner_canSubmitBuildingChange() throws Exception {
        String created = createFamilyAs(OWNER, "4", "1", "401");
        String familyId = json(created).path("data").path("id").asText();
        mockMvc.perform(post("/families/" + familyId + "/members")
                        .header("Authorization", bearer(OWNER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13950000002\",\"relation\":\"spouse\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/residents/me/building-changes")
                        .header("Authorization", bearer(OWNER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"communityId\":\"" + COM + "\",\"building\":\"9\","
                                + "\"unit\":\"1\",\"floor\":\"9\",\"room\":\"901\"}"))
                .andExpect(status().isOk());
    }

    // ==================== ② 添加成员时写入家庭地址 ====================

    @Test
    @DisplayName("户主添加家庭成员：成员住址随家庭落库，不再是「无房号住户」")
    void addMember_inheritsFamilyAddress() throws Exception {
        String created = createFamilyAs(OWNER, "4", "1", "401");
        String familyId = json(created).path("data").path("id").asText();

        mockMvc.perform(post("/families/" + familyId + "/members")
                        .header("Authorization", bearer(OWNER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13950000002\",\"relation\":\"spouse\"}"))
                .andExpect(status().isOk());

        Resident spouse = reload(SPOUSE);
        assertEquals(COM, spouse.getCommunityId());
        assertEquals("4", spouse.getBuilding());
        assertEquals("1", spouse.getUnit());
        assertEquals("401", spouse.getRoom(), "成员地址必须随家庭，否则定向通告/邻居列表查不到他");
        assertEquals(familyId, spouse.getFamilyId());
    }

    // ==================== ③ 家庭地址变更同步全部成员 ====================

    @Test
    @DisplayName("户主改家庭住址：全部成员住址一起改")
    void updateFamilyAddress_syncsAllMembers() throws Exception {
        String created = createFamilyAs(OWNER, "4", "1", "401");
        String familyId = json(created).path("data").path("id").asText();
        mockMvc.perform(post("/families/" + familyId + "/members")
                        .header("Authorization", bearer(OWNER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13950000002\",\"relation\":\"spouse\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/families/me")
                        .header("Authorization", bearer(OWNER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"building\":\"6\",\"unit\":\"3\",\"room\":\"603\"}"))
                .andExpect(status().isOk());

        assertEquals("603", reload(OWNER).getRoom());
        assertEquals("603", reload(SPOUSE).getRoom(), "只改 families 会让成员留旧房号，冲突原样复现");
        Family family = familyRepository.findById(familyId).orElseThrow();
        assertEquals("6", family.getBuilding());
    }

    @Test
    @DisplayName("户主把家庭改到别人已占的房号 → 400，住址不被改写")
    void updateFamilyAddress_intoOtherFamilyRoom_isRejected() throws Exception {
        createFamilyAs(STRANGER, "5", "2", "502");
        String created = createFamilyAs(OWNER, "4", "1", "401");
        String familyId = json(created).path("data").path("id").asText();

        mockMvc.perform(put("/families/me")
                        .header("Authorization", bearer(OWNER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"building\":\"5\",\"unit\":\"2\",\"room\":\"502\"}"))
                .andExpect(status().isBadRequest());

        assertEquals("401", reload(OWNER).getRoom());
    }

    @Test
    @DisplayName("审批通过时复检占用并同步成员；期间被他人占住则审批失败")
    void approvalRevalidatesOccupancyAndSyncsMembers() throws Exception {
        String created = createFamilyAs(OWNER, "4", "1", "401");
        String familyId = json(created).path("data").path("id").asText();
        mockMvc.perform(post("/families/" + familyId + "/members")
                        .header("Authorization", bearer(OWNER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13950000002\",\"relation\":\"spouse\"}"))
                .andExpect(status().isOk());

        String applied = contentOf(mockMvc.perform(post("/residents/me/building-changes")
                        .header("Authorization", bearer(OWNER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"communityId\":\"" + COM + "\",\"building\":\"7\","
                                + "\"unit\":\"1\",\"floor\":\"7\",\"room\":\"701\"}"))
                .andExpect(status().isOk())
                .andReturn());
        String requestId = json(applied).path("data").path("id").asText();

        // 申请挂起期间该房号被他人建了家庭：审批必须复检并拒绝，不能写进两个家庭
        createFamilyAs(STRANGER, "7", "1", "701");

        mockMvc.perform(post("/admin/building-changes/" + requestId + "/approve")
                        .header("Authorization", bearer(ADMIN, "property_admin", PC, null)))
                .andExpect(status().isBadRequest());
        assertEquals("401", reload(OWNER).getRoom(), "审批失败不得改动原住址");

        // 申请仍是待审批（同一住户只允许一条），先驳回才能再提
        mockMvc.perform(post("/admin/building-changes/" + requestId + "/reject")
                        .param("reason", "房号已被其他家庭占用")
                        .header("Authorization", bearer(ADMIN, "property_admin", PC, null)))
                .andExpect(status().isOk());

        // 换成真正空房后审批通过：户主与成员住址一起同步
        String applied2 = contentOf(mockMvc.perform(post("/residents/me/building-changes")
                        .header("Authorization", bearer(OWNER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"communityId\":\"" + COM + "\",\"building\":\"8\","
                                + "\"unit\":\"1\",\"floor\":\"8\",\"room\":\"801\"}"))
                .andExpect(status().isOk())
                .andReturn());
        String requestId2 = json(applied2).path("data").path("id").asText();

        mockMvc.perform(post("/admin/building-changes/" + requestId2 + "/approve")
                        .header("Authorization", bearer(ADMIN, "property_admin", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.room").value("801"));

        assertEquals("801", reload(OWNER).getRoom());
        assertEquals("801", reload(SPOUSE).getRoom());
        assertEquals("8", reload(SPOUSE).getBuilding());
        assertEquals("801", familyRepository.findById(familyId).orElseThrow().getRoom());
    }
}
