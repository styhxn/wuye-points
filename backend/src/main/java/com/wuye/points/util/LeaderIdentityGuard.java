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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 负责人身份解析（2026-10-20 负责人标签化改造；2026-10-21 扩展活动组组长）。
 *
 * <p>统筹 / 板块负责人 / 个体负责人 / 活动组组长不再是「与业主住户互斥的角色字段」，而是
 * 「住户 + 身份」复合身份（标签）：{@code residents.role} 保持 {@code resident} 不变，
 * 身份由各业务表的 active 记录判定——与单元楼长（building_leaders）、
 * 物业经理（property_managers）完全同模式。</p>
 *
 * <p>本组件同时兼容存量数据 / 存量 JWT 中角色值仍为 {@code coordinator} / {@code activity_leader}
 * 等的情况：判定身份时「JWT 角色值 或 业务表 active 记录」任一命中即视为持有该身份。</p>
 */
@Component
@RequiredArgsConstructor
public class LeaderIdentityGuard {

    public static final String ROLE_COORDINATOR = "coordinator";
    public static final String ROLE_SECTOR_LEADER = "sector_leader";
    public static final String ROLE_INDIVIDUAL_LEADER = "individual_leader";
    public static final String ROLE_ACTIVITY_LEADER = "activity_leader";

    private final CoordinatorRepository coordinatorRepository;
    private final SectorLeaderRepository sectorLeaderRepository;
    private final IndividualLeaderRepository individualLeaderRepository;
    private final ActivityLeaderIdentityRepository activityLeaderIdentityRepository;

    // ------------------------------------------------------------------ 按住户解析

    public Optional<Coordinator> activeCoordinator(String residentId, String propertyCompanyId) {
        if (residentId == null || propertyCompanyId == null) {
            return Optional.empty();
        }
        return coordinatorRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                residentId, propertyCompanyId, "active");
    }

    /** 同一住户可跨板块持有多个启用中的板块负责人记录 */
    public List<SectorLeader> activeSectorLeaders(String residentId, String propertyCompanyId) {
        if (residentId == null || propertyCompanyId == null) {
            return List.of();
        }
        return sectorLeaderRepository.findAllByResidentIdAndPropertyCompanyIdAndStatus(
                residentId, propertyCompanyId, "active");
    }

    public Optional<IndividualLeader> activeIndividualLeader(String residentId, String propertyCompanyId) {
        if (residentId == null || propertyCompanyId == null) {
            return Optional.empty();
        }
        return individualLeaderRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                residentId, propertyCompanyId, "active");
    }

    /** 活动组组长身份（2026-10-21 标签化：由 activity_leader_identities 表 active 记录判定） */
    public Optional<ActivityLeaderIdentity> activeActivityLeader(String residentId, String propertyCompanyId) {
        if (residentId == null || propertyCompanyId == null) {
            return Optional.empty();
        }
        return activityLeaderIdentityRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                        residentId, propertyCompanyId, "active");
    }

    /** 是否持有任意一种启用中的负责人身份（不含活动组组长） */
    public boolean hasAnyLeaderIdentity(String residentId, String propertyCompanyId) {
        return activeCoordinator(residentId, propertyCompanyId).isPresent()
                || !activeSectorLeaders(residentId, propertyCompanyId).isEmpty()
                || activeIndividualLeader(residentId, propertyCompanyId).isPresent();
    }

    /** 住户持有的全部负责人角色码（供消息广播按角色覆盖、权限预设解析等场景使用） */
    public Set<String> leaderRoleCodes(String residentId, String propertyCompanyId) {
        Set<String> codes = new HashSet<>();
        if (activeCoordinator(residentId, propertyCompanyId).isPresent()) {
            codes.add(ROLE_COORDINATOR);
        }
        if (!activeSectorLeaders(residentId, propertyCompanyId).isEmpty()) {
            codes.add(ROLE_SECTOR_LEADER);
        }
        if (activeIndividualLeader(residentId, propertyCompanyId).isPresent()) {
            codes.add(ROLE_INDIVIDUAL_LEADER);
        }
        if (activeActivityLeader(residentId, propertyCompanyId).isPresent()) {
            codes.add(ROLE_ACTIVITY_LEADER);
        }
        return codes;
    }

    // ------------------------------------------------------------------ 按当前登录用户解析

    /** 当前用户是否为启用中的统筹（兼容存量 JWT role=coordinator） */
    public boolean isCurrentCoordinator() {
        if (ROLE_COORDINATOR.equals(UserContext.currentRole())) {
            return true;
        }
        return activeCoordinator(UserContext.currentUserId(), UserContext.currentPropertyCompanyId()).isPresent();
    }

    /** 当前用户是否为启用中的板块负责人（兼容存量 JWT role=sector_leader） */
    public boolean isCurrentSectorLeader() {
        if (ROLE_SECTOR_LEADER.equals(UserContext.currentRole())) {
            return true;
        }
        return !activeSectorLeaders(UserContext.currentUserId(), UserContext.currentPropertyCompanyId()).isEmpty();
    }

    /** 当前用户是否为启用中的个体负责人（兼容存量 JWT role=individual_leader） */
    public boolean isCurrentIndividualLeader() {
        if (ROLE_INDIVIDUAL_LEADER.equals(UserContext.currentRole())) {
            return true;
        }
        return activeIndividualLeader(UserContext.currentUserId(), UserContext.currentPropertyCompanyId()).isPresent();
    }

    /** 当前用户是否为启用中的活动组组长（兼容存量 JWT role=activity_leader） */
    public boolean isCurrentActivityLeader() {
        if (ROLE_ACTIVITY_LEADER.equals(UserContext.currentRole())) {
            return true;
        }
        return activeActivityLeader(UserContext.currentUserId(), UserContext.currentPropertyCompanyId()).isPresent();
    }

    /**
     * 当前用户的有效角色码：持有负责人/组长身份时返回对应角色码（用于审计字段、
     * publisherRole / applicantRole 等留痕场景，保持与改造前一致的记录值）；
     * 否则返回 JWT 中的角色值。优先级：统筹 &gt; 板块负责人 &gt; 个体负责人 &gt; 活动组组长。
     */
    public String currentEffectiveRole() {
        String role = UserContext.currentRole();
        if (ROLE_COORDINATOR.equals(role) || ROLE_SECTOR_LEADER.equals(role)
                || ROLE_INDIVIDUAL_LEADER.equals(role) || ROLE_ACTIVITY_LEADER.equals(role)) {
            return role;
        }
        String userId = UserContext.currentUserId();
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        if (activeCoordinator(userId, propertyCompanyId).isPresent()) {
            return ROLE_COORDINATOR;
        }
        if (!activeSectorLeaders(userId, propertyCompanyId).isEmpty()) {
            return ROLE_SECTOR_LEADER;
        }
        if (activeIndividualLeader(userId, propertyCompanyId).isPresent()) {
            return ROLE_INDIVIDUAL_LEADER;
        }
        if (activeActivityLeader(userId, propertyCompanyId).isPresent()) {
            return ROLE_ACTIVITY_LEADER;
        }
        return role;
    }
}
