package com.wuye.points.controller;

import com.wuye.points.entity.BuildingChangeRequest;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.CommunityRoom;
import com.wuye.points.entity.Family;
import com.wuye.points.entity.NeighborBlock;
import com.wuye.points.entity.NeighborContactLimit;
import com.wuye.points.entity.NavigationItem;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.BuildingChangeRequestRepository;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.CommunityRoomRepository;
import com.wuye.points.repository.FamilyRepository;
import com.wuye.points.repository.NeighborBlockRepository;
import com.wuye.points.repository.NeighborContactLimitRepository;
import com.wuye.points.repository.NavigationItemRepository;
import com.wuye.points.repository.ResidentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 整改清单（后端）账号注销、邻居聊天、导航与房号口径的回归测试。
 * 覆盖条目 9/10/12/27/28/31/32。
 */
@DisplayName("整改清单回归：注销/聊天/导航/房号")
class RectificationAccountChatNavTest extends ControllerTestBase {

    private static final String PC = "pc_rect0002";
    private static final String COMMUNITY = "com_rect0002";
    private static final String BLOCKER = "res_rect_blocker";
    private static final String BLOCKED = "res_rect_blocked";
    private static final String DELETING = "res_rect_deleting";
    private static final String PLAIN = "res_rect_plain";
    private static final String ADMIN = "res_rect_admin2";
    private static final String PLATFORM = "res_rect_platform2";

    @Autowired
    private ResidentRepository residentRepository;
    @Autowired
    private CommunityRepository communityRepository;
    @Autowired
    private CommunityRoomRepository communityRoomRepository;
    @Autowired
    private NavigationItemRepository navigationItemRepository;
    @Autowired
    private NeighborBlockRepository neighborBlockRepository;
    @Autowired
    private NeighborContactLimitRepository neighborContactLimitRepository;
    @Autowired
    private FamilyRepository familyRepository;
    @Autowired
    private BuildingChangeRequestRepository buildingChangeRequestRepository;

    /** 带 token_version 的令牌：会走 AuthInterceptor 的账号状态校验（冷静期/注销的真实表现）。 */
    private String bearerWithVersion(String userId, String role, Integer tokenVersion) {
        return "Bearer " + jwtUtil.generateAccessToken(userId, role, PC, null, tokenVersion);
    }

    private Resident resident(String id, String phone, String role, String room) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setPhone(phone);
        resident.setName("整改测试" + id.substring(4));
        resident.setRole(role);
        resident.setStatus("active");
        resident.setPropertyCompanyId(PC);
        resident.setCommunityId(COMMUNITY);
        resident.setBuilding("1");
        resident.setUnit("1");
        resident.setRoom(room);
        resident.setPointBalance(0);
        resident.setCoinBalance(BigDecimal.ZERO);
        resident.setTokenVersion(0);
        return residentRepository.save(resident);
    }

    @AfterEach
    void cleanUp() {
        // 只清本类自己造的数据：H2 在整个测试套里是同一个库，deleteAll 会连带删掉别的用例的数据
        neighborBlockRepository.deleteAll(
                neighborBlockRepository.findByBlockerIdOrderByBlockedAtDesc(BLOCKER));
        neighborContactLimitRepository.findByResidentIdAndContactDate(
                BLOCKER, LocalDate.now(java.time.ZoneId.of("Asia/Shanghai")))
                .ifPresent(neighborContactLimitRepository::delete);
        communityRoomRepository.deleteAllById(List.of("room_rect_108", "room_rect_109"));
        navigationItemRepository.deleteAllById(List.of("nav_rect0001"));
        buildingChangeRequestRepository.deleteAllById(List.of("bcr_rect0001"));
        familyRepository.deleteAllById(List.of("fam_rect0001"));
        residentRepository.deleteAllById(
                List.of(BLOCKER, BLOCKED, DELETING, PLAIN, ADMIN, PLATFORM));
        communityRepository.deleteById(COMMUNITY);
    }

    // ==================== 条目9/10：注销冷静期与审核 ====================

    @Test
    @DisplayName("条目9：提交注销申请后账号仍可登录使用，且 deleted_at 不再被当作冷静期时间")
    void deletionRequest_keepsAccountUsable() throws Exception {
        Resident target = resident(DELETING, "13920000001", "resident", "201");

        mockMvc.perform(post("/residents/me/delete-request")
                        .header("Authorization", bearerWithVersion(DELETING, "resident", target.getTokenVersion()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmText\":\"确认注销\",\"reason\":\"测试\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("pending"));

        Resident after = residentRepository.findById(DELETING).orElseThrow();
        assertEquals("pending_deletion", after.getStatus());
        assertNull(after.getDeletedAt(), "冷静期不得写 deleted_at");
        assertNotNull(after.getDeletionExpiresAt());
        assertTrue(after.getDeletionExpiresAt().isAfter(LocalDateTime.now().plusDays(14)));

        // 关键回归：冷静期内带 tv 的令牌仍可用（此前被 AuthInterceptor 判「账号被禁用」20005）
        mockMvc.perform(get("/residents/me")
                        .header("Authorization", bearerWithVersion(DELETING, "resident", after.getTokenVersion())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("条目9：冷静期内可自助撤回，账号恢复正常")
    void deletionRequest_canBeCancelledByResident() throws Exception {
        resident(DELETING, "13920000001", "resident", "201");
        mockMvc.perform(post("/residents/me/delete-request")
                        .header("Authorization", bearer(DELETING, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmText\":\"确认注销\",\"reason\":\"测试\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/residents/me/delete-request/cancel")
                        .header("Authorization", bearer(DELETING, "resident", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("active"));

        Resident after = residentRepository.findById(DELETING).orElseThrow();
        assertEquals("active", after.getStatus());
        assertNull(after.getDeletionExpiresAt());
    }

    @Test
    @DisplayName("条目10：管理端审核通过 → 账号标记删除且令牌失效（此前必 404）")
    void deletionReview_approve_finalizesDeletion() throws Exception {
        Resident target = resident(DELETING, "13920000001", "resident", "201");
        mockMvc.perform(post("/residents/me/delete-request")
                        .header("Authorization", bearer(DELETING, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmText\":\"确认注销\",\"reason\":\"测试\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/admin/residents/" + DELETING + "/deletion/review")
                        .header("Authorization", bearer(PLATFORM, "platform_admin", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"approve\"}"))
                .andExpect(status().isOk());

        Resident after = residentRepository.findById(DELETING).orElseThrow();
        assertEquals("deleted", after.getStatus());
        assertNotNull(after.getDeletedAt());
        assertEquals(target.getTokenVersion() + 1, after.getTokenVersion(), "注销须让旧令牌版本失效");

        // 注销后用旧令牌调接口 → 账号被禁用
        mockMvc.perform(get("/residents/me")
                        .header("Authorization", bearerWithVersion(DELETING, "resident", target.getTokenVersion())))
                .andExpect(status().isForbidden());

        // 重复审核幂等成功，不再报「不处于注销申请状态」
        mockMvc.perform(post("/admin/residents/" + DELETING + "/deletion/review")
                        .header("Authorization", bearer(PLATFORM, "platform_admin", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"approve\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("条目10：驳回后恢复 active 并清空冷静期")
    void deletionReview_reject_restoresAccount() throws Exception {
        resident(DELETING, "13920000001", "resident", "201");
        mockMvc.perform(post("/residents/me/delete-request")
                        .header("Authorization", bearer(DELETING, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmText\":\"确认注销\",\"reason\":\"测试\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/admin/residents/" + DELETING + "/deletion/review")
                        .header("Authorization", bearer(PLATFORM, "platform_admin", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"reject\",\"rejectReason\":\"资料未补齐\"}"))
                .andExpect(status().isOk());

        Resident after = residentRepository.findById(DELETING).orElseThrow();
        assertEquals("active", after.getStatus());
        assertNull(after.getDeletionExpiresAt());
    }

    @Test
    @DisplayName("条目9：注销申请列表按 status 查得到（冷静期改列后仍不能漏）")
    void deletionRequestList_showsPendingAccounts() throws Exception {
        resident(DELETING, "13920000001", "resident", "201");
        mockMvc.perform(post("/residents/me/delete-request")
                        .header("Authorization", bearer(DELETING, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmText\":\"确认注销\",\"reason\":\"测试\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/residents/deletion-requests")
                        .header("Authorization", bearer(PLATFORM, "platform_admin", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[?(@.id == '" + DELETING + "')].deletionExpiresAt",
                        org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    @DisplayName("条目9：冷静期内重复提交注销申请 → 400，不会把到期时间往后推")
    void deletionRequest_isNotRepeatableDuringCoolingPeriod() throws Exception {
        resident(DELETING, "13920000001", "resident", "201");
        mockMvc.perform(post("/residents/me/delete-request")
                        .header("Authorization", bearer(DELETING, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmText\":\"确认注销\",\"reason\":\"测试\"}"))
                .andExpect(status().isOk());

        LocalDateTime firstExpiry = residentRepository.findById(DELETING)
                .orElseThrow().getDeletionExpiresAt();

        mockMvc.perform(post("/residents/me/delete-request")
                        .header("Authorization", bearer(DELETING, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmText\":\"确认注销\",\"reason\":\"再试一次\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("已有待审核的注销申请")));

        assertEquals(firstExpiry, residentRepository.findById(DELETING)
                .orElseThrow().getDeletionExpiresAt(), "重复提交不得顺延冷静期");
    }

    // ==================== 条目27/28：私聊限额与屏蔽 ====================

    @Test
    @DisplayName("条目28：单向屏蔽——被屏蔽方发不出（90016），屏蔽方仍可发；名单接口能查回")
    void blockedNeighbor_cannotSendButBlockerCan() throws Exception {
        resident(BLOCKER, "13920000011", "resident", "301");
        resident(BLOCKED, "13920000012", "resident", "302");

        mockMvc.perform(post("/messages/neighbors/block")
                        .header("Authorization", bearer(BLOCKER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"blockedId\":\"" + BLOCKED + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/messages/neighbor-chat")
                        .header("Authorization", bearer(BLOCKED, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toId\":\"" + BLOCKER + "\",\"content\":\"你好\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(90016));

        mockMvc.perform(post("/messages/neighbor-chat")
                        .header("Authorization", bearer(BLOCKER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toId\":\"" + BLOCKED + "\",\"content\":\"在吗\"}"))
                .andExpect(status().isOk());

        // 条目28：重登后要能恢复屏蔽状态（此前后端没有任何查询接口）
        mockMvc.perform(get("/messages/neighbors/blocked")
                        .header("Authorization", bearer(BLOCKER, "resident", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.residentId == '" + BLOCKED + "')]",
                        org.hamcrest.Matchers.hasSize(1)));

        mockMvc.perform(get("/messages/neighbors")
                        .param("page", "1").param("pageSize", "20")
                        .header("Authorization", bearer(BLOCKER, "resident", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[?(@.residentId == '" + BLOCKED + "')].isBlocked",
                        org.hamcrest.Matchers.hasItem(true)));
    }

    @Test
    @DisplayName("条目27：当天总量限额用完后，改走 /messages 也发不出去（此前可绕过）")
    void dailyQuota_appliesToBothChatEndpoints() throws Exception {
        resident(BLOCKER, "13920000011", "resident", "301");
        resident(BLOCKED, "13920000012", "resident", "302");

        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/messages/neighbor-chat")
                            .header("Authorization", bearer(BLOCKER, "resident", PC, null))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"toId\":\"" + BLOCKED + "\",\"content\":\"第" + i + "条\"}"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/messages/neighbor-chat")
                        .header("Authorization", bearer(BLOCKER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toId\":\"" + BLOCKED + "\",\"content\":\"第4条\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(90013));

        // 前端被 90013 挡掉后会自动改走 /messages（community_message + private），同样要拦住
        mockMvc.perform(post("/messages")
                        .header("Authorization", bearer(BLOCKER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"messageType\":\"community_message\",\"chatType\":\"private\","
                                + "\"toId\":\"" + BLOCKED + "\",\"content\":\"换个接口发\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(90013));

        mockMvc.perform(get("/messages/contact-quota")
                        .header("Authorization", bearer(BLOCKER, "resident", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dailyLimit").value(3))
                .andExpect(jsonPath("$.data.usedToday").value(3))
                .andExpect(jsonPath("$.data.remaining").value(0));
    }

    // ==================== 条目31：导航项下发口径 ====================

    @Test
    @DisplayName("条目31：只传 module 时，管理端配好并启用的 top 位导航项也要下发到首页")
    void navigation_topItemVisibleWithoutPosition() throws Exception {
        resident(PLAIN, "13920000021", "resident", "401");
        NavigationItem item = new NavigationItem();
        item.setId("nav_rect0001");
        item.setPropertyCompanyId(PC);
        item.setPosition("top");
        item.setModule("home");
        item.setTitle("公告");
        item.setIcon("notice");
        item.setLinkType("page");
        item.setLinkUrl("/pages/announcement/list");
        item.setSortOrder(1);
        item.setEnabled(true);
        navigationItemRepository.save(item);

        mockMvc.perform(get("/navigation-items")
                        .param("module", "home")
                        .header("Authorization", bearer(PLAIN, "resident", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.title == '公告')]", org.hamcrest.Matchers.hasSize(1)));
    }

    // ==================== 条目32：可用房号不再列出已入住 ====================

    @Test
    @DisplayName("条目32：只传楼栋时，已绑定住户的 108 不得出现在可用房号里")
    void availableRooms_excludeOccupiedRoom() throws Exception {
        resident(PLAIN, "13920000021", "resident", "108");
        Community community = new Community();
        community.setId(COMMUNITY);
        community.setPropertyCompanyId(PC);
        community.setName("整改测试小区");
        community.setStatus("active");
        communityRepository.save(community);

        communityRoomRepository.save(room("room_rect_108", "108", "occupied", PLAIN));
        communityRoomRepository.save(room("room_rect_109", "109", "vacant", null));

        mockMvc.perform(get("/communities/" + COMMUNITY + "/available-rooms")
                        .param("building", "1")
                        .header("Authorization", bearer(ADMIN, "property_admin", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.room == '108')]", org.hamcrest.Matchers.hasSize(0)))
                .andExpect(jsonPath("$.data.items[?(@.room == '109')]", org.hamcrest.Matchers.hasSize(1)));
    }

    private CommunityRoom room(String id, String roomNo, String status, String residentId) {
        CommunityRoom room = new CommunityRoom();
        room.setId(id);
        room.setCommunityId(COMMUNITY);
        room.setBuilding("1");
        room.setUnit("1");
        room.setFloor("1");
        room.setRoom(roomNo);
        room.setStatus(status);
        room.setResidentId(residentId);
        return room;
    }

    // ==================== 条目12：楼栋变更审批后同步家庭地址 ====================

    @Test
    @DisplayName("条目12/44：户主楼栋变更审批通过 → families 地址同步，家庭不再判成「不是同一个房号」")
    void buildingChangeApproval_syncsFamilyAddress() throws Exception {
        Resident owner = resident(PLAIN, "13920000021", "resident", "501");
        owner.setFamilyId("fam_rect0001");
        residentRepository.save(owner);

        Family family = new Family();
        family.setId("fam_rect0001");
        family.setName("整改测试家庭");
        family.setOwnerId(PLAIN);
        family.setPropertyCompanyId(PC);
        family.setCommunityId(COMMUNITY);
        family.setBuilding("1");
        family.setUnit("1");
        family.setRoom("501");
        family.setMemberCount(1);
        family.setPointBalance(0);
        family.setCoinBalance(BigDecimal.ZERO);
        family.setAuditStatus("approved");
        family.setStatus("active");
        familyRepository.save(family);

        BuildingChangeRequest change = new BuildingChangeRequest();
        change.setId("bcr_rect0001");
        change.setResidentId(PLAIN);
        change.setPropertyCompanyId(PC);
        change.setCommunityId(COMMUNITY);
        change.setBuilding("2");
        change.setUnit("3");
        change.setFloor("12");
        change.setRoom("1203");
        change.setStatus("pending");
        change.setAppliedAt(LocalDateTime.now());
        buildingChangeRequestRepository.save(change);

        mockMvc.perform(post("/admin/building-changes/bcr_rect0001/approve")
                        .header("Authorization", bearer(ADMIN, "property_admin", PC, null)))
                .andExpect(status().isOk());

        Family after = familyRepository.findById("fam_rect0001").orElseThrow();
        assertEquals("2", after.getBuilding());
        assertEquals("3", after.getUnit());
        assertEquals("1203", after.getRoom());
    }
}
