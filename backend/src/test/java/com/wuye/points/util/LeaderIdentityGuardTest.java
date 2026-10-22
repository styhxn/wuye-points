package com.wuye.points.util;

import com.wuye.points.context.UserContext;
import com.wuye.points.entity.ActivityLeaderIdentity;
import com.wuye.points.entity.Coordinator;
import com.wuye.points.entity.IndividualLeader;
import com.wuye.points.entity.SectorLeader;
import com.wuye.points.repository.ActivityLeaderIdentityRepository;
import com.wuye.points.repository.CoordinatorRepository;
import com.wuye.points.repository.IndividualLeaderRepository;
import com.wuye.points.repository.SectorLeaderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 负责人身份解析测试（2026-10-20 负责人标签化；2026-10-21 扩展活动组组长）。
 */
@DisplayName("LeaderIdentityGuard 负责人身份解析测试")
class LeaderIdentityGuardTest {

    private CoordinatorRepository coordinatorRepository;
    private SectorLeaderRepository sectorLeaderRepository;
    private IndividualLeaderRepository individualLeaderRepository;
    private ActivityLeaderIdentityRepository activityLeaderIdentityRepository;
    private LeaderIdentityGuard guard;

    @BeforeEach
    void setUp() {
        coordinatorRepository = mock(CoordinatorRepository.class);
        sectorLeaderRepository = mock(SectorLeaderRepository.class);
        individualLeaderRepository = mock(IndividualLeaderRepository.class);
        activityLeaderIdentityRepository = mock(ActivityLeaderIdentityRepository.class);
        guard = new LeaderIdentityGuard(coordinatorRepository, sectorLeaderRepository,
                individualLeaderRepository, activityLeaderIdentityRepository);
    }

    @AfterEach
    void cleanup() {
        UserContext.clear();
    }

    private void setUserContext(String role) {
        UserContext.set(UserContext.builder()
                .userId("res_test001")
                .role(role)
                .propertyCompanyId("pc_001")
                .familyId("fam_test001")
                .build());
    }

    @Test
    @DisplayName("按 active 表记录判定统筹身份")
    void activeCoordinator_shouldResolveFromTable() {
        when(coordinatorRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                "res_001", "pc_001", "active"))
                .thenReturn(Optional.of(mock(Coordinator.class)));
        assertTrue(guard.activeCoordinator("res_001", "pc_001").isPresent());
        assertFalse(guard.activeCoordinator("res_002", "pc_001").isPresent());
        // 空入参安全
        assertFalse(guard.activeCoordinator(null, "pc_001").isPresent());
        assertFalse(guard.activeCoordinator("res_001", null).isPresent());
    }

    @Test
    @DisplayName("同一住户可跨板块持有多个板块负责人记录")
    void activeSectorLeaders_shouldReturnAllActiveRecords() {
        when(sectorLeaderRepository.findAllByResidentIdAndPropertyCompanyIdAndStatus(
                "res_001", "pc_001", "active"))
                .thenReturn(List.of(mock(SectorLeader.class), mock(SectorLeader.class)));
        assertEquals(2, guard.activeSectorLeaders("res_001", "pc_001").size());
        assertTrue(guard.activeSectorLeaders(null, "pc_001").isEmpty());
    }

    @Test
    @DisplayName("hasAnyLeaderIdentity - 任一负责人身份命中即为真")
    void hasAnyLeaderIdentity_shouldCoverAllThreeTypes() {
        when(individualLeaderRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                "res_il", "pc_001", "active"))
                .thenReturn(Optional.of(mock(IndividualLeader.class)));
        when(sectorLeaderRepository.findAllByResidentIdAndPropertyCompanyIdAndStatus(
                "res_sl", "pc_001", "active"))
                .thenReturn(List.of(mock(SectorLeader.class)));
        when(coordinatorRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                "res_coo", "pc_001", "active"))
                .thenReturn(Optional.of(mock(Coordinator.class)));

        assertTrue(guard.hasAnyLeaderIdentity("res_coo", "pc_001"));
        assertTrue(guard.hasAnyLeaderIdentity("res_sl", "pc_001"));
        assertTrue(guard.hasAnyLeaderIdentity("res_il", "pc_001"));
        assertFalse(guard.hasAnyLeaderIdentity("res_plain", "pc_001"));
    }

    @Test
    @DisplayName("isCurrentCoordinator - 兼容存量 JWT role=coordinator")
    void isCurrentCoordinator_shouldAcceptLegacyJwtRole() {
        setUserContext("coordinator");
        assertTrue(guard.isCurrentCoordinator());
    }

    @Test
    @DisplayName("isCurrentSectorLeader / isCurrentIndividualLeader - 表记录判定")
    void isCurrentLeader_shouldResolveFromTable() {
        setUserContext("resident");
        when(sectorLeaderRepository.findAllByResidentIdAndPropertyCompanyIdAndStatus(
                "res_test001", "pc_001", "active"))
                .thenReturn(List.of(mock(SectorLeader.class)));
        assertTrue(guard.isCurrentSectorLeader());
        assertFalse(guard.isCurrentIndividualLeader());

        when(individualLeaderRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                "res_test001", "pc_001", "active"))
                .thenReturn(Optional.of(mock(IndividualLeader.class)));
        assertTrue(guard.isCurrentIndividualLeader());
    }

    @Test
    @DisplayName("currentEffectiveRole - 优先级 统筹 > 板块 > 个体 > 组长，否则原角色")
    void currentEffectiveRole_shouldFollowPriority() {
        setUserContext("resident");
        when(coordinatorRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                anyString(), anyString(), eq("active")))
                .thenReturn(Optional.of(mock(Coordinator.class)));
        assertEquals("coordinator", guard.currentEffectiveRole());

        when(coordinatorRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                anyString(), anyString(), eq("active")))
                .thenReturn(Optional.empty());
        when(sectorLeaderRepository.findAllByResidentIdAndPropertyCompanyIdAndStatus(
                anyString(), anyString(), eq("active")))
                .thenReturn(List.of(mock(SectorLeader.class)));
        assertEquals("sector_leader", guard.currentEffectiveRole());

        when(sectorLeaderRepository.findAllByResidentIdAndPropertyCompanyIdAndStatus(
                anyString(), anyString(), eq("active")))
                .thenReturn(List.of());
        when(individualLeaderRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                anyString(), anyString(), eq("active")))
                .thenReturn(Optional.of(mock(IndividualLeader.class)));
        assertEquals("individual_leader", guard.currentEffectiveRole());

        when(individualLeaderRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                anyString(), anyString(), eq("active")))
                .thenReturn(Optional.empty());
        when(activityLeaderIdentityRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                anyString(), anyString(), eq("active")))
                .thenReturn(Optional.of(mock(ActivityLeaderIdentity.class)));
        assertEquals("activity_leader", guard.currentEffectiveRole());

        when(activityLeaderIdentityRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                anyString(), anyString(), eq("active")))
                .thenReturn(Optional.empty());
        assertEquals("resident", guard.currentEffectiveRole());
    }

    @Test
    @DisplayName("currentEffectiveRole - 存量 JWT 角色值优先原样返回")
    void currentEffectiveRole_shouldKeepLegacyJwtRole() {
        setUserContext("sector_leader");
        assertEquals("sector_leader", guard.currentEffectiveRole());
    }

    @Test
    @DisplayName("currentEffectiveRole - 存量 JWT role=activity_leader 原样返回")
    void currentEffectiveRole_shouldKeepLegacyActivityLeaderRole() {
        setUserContext("activity_leader");
        assertEquals("activity_leader", guard.currentEffectiveRole());
    }

    @Test
    @DisplayName("leaderRoleCodes - 返回住户持有的全部负责人角色码")
    void leaderRoleCodes_shouldReturnAllHeldCodes() {
        when(coordinatorRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                "res_multi", "pc_001", "active"))
                .thenReturn(Optional.of(mock(Coordinator.class)));
        when(sectorLeaderRepository.findAllByResidentIdAndPropertyCompanyIdAndStatus(
                "res_multi", "pc_001", "active"))
                .thenReturn(List.of(mock(SectorLeader.class)));
        when(individualLeaderRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                "res_multi", "pc_001", "active"))
                .thenReturn(Optional.of(mock(IndividualLeader.class)));

        Set<String> codes = guard.leaderRoleCodes("res_multi", "pc_001");
        assertEquals(Set.of("coordinator", "sector_leader", "individual_leader"), codes);
    }

    @Test
    @DisplayName("leaderRoleCodes - 持有组长身份时包含 activity_leader（2026-10-21 标签化）")
    void leaderRoleCodes_shouldIncludeActivityLeader() {
        when(activityLeaderIdentityRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                "res_act", "pc_001", "active"))
                .thenReturn(Optional.of(mock(ActivityLeaderIdentity.class)));
        Set<String> codes = guard.leaderRoleCodes("res_act", "pc_001");
        assertEquals(Set.of("activity_leader"), codes);
    }

    @Test
    @DisplayName("isCurrentActivityLeader - 兼容存量 JWT role=activity_leader")
    void isCurrentActivityLeader_shouldAcceptLegacyJwtRole() {
        setUserContext("activity_leader");
        assertTrue(guard.isCurrentActivityLeader());
    }

    @Test
    @DisplayName("isCurrentActivityLeader - 表记录判定（role=resident + active 身份）")
    void isCurrentActivityLeader_shouldResolveFromTable() {
        setUserContext("resident");
        when(activityLeaderIdentityRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                "res_test001", "pc_001", "active"))
                .thenReturn(Optional.of(mock(ActivityLeaderIdentity.class)));
        assertTrue(guard.isCurrentActivityLeader());

        when(activityLeaderIdentityRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                "res_test001", "pc_001", "active"))
                .thenReturn(Optional.empty());
        assertFalse(guard.isCurrentActivityLeader());
    }

    @Test
    @DisplayName("activeActivityLeader - 空入参安全")
    void activeActivityLeader_shouldRejectNullArgs() {
        assertFalse(guard.activeActivityLeader(null, "pc_001").isPresent());
        assertFalse(guard.activeActivityLeader("res_001", null).isPresent());
    }
}
