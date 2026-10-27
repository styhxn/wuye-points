package com.wuye.points.util;

import com.wuye.points.context.UserContext;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import org.springframework.stereotype.Component;

import org.springframework.util.StringUtils;
import java.util.Arrays;
import java.util.Set;

/**
 * 角色与数据范围校验
 */
@Component
public class RoleGuard {

    /**
     * 历史 JWT 兼容：早期物业子账号把 property_leader / property_operator 直接写进 JWT role，
     * 现行模型为 property_admin + propertySubRole，此处归一化后按 property_admin 校验。
     * （RoleGuardTest.requireAnyRole_shouldPass_whenJwtRoleIsPropertyLeader/Operator）
     */
    private static final Set<String> LEGACY_PROPERTY_ADMIN_ROLES =
            Set.of("property_leader", "property_operator");

    /**
     * 住户子角色：本质是「特殊住户 + 子身份」，其住户身份不应丢失。
     * 存量数据/令牌中 role 可能仍为这些值，此处归一化为 resident，
     * 保证所有 requireAnyRole("resident") 的住户功能继续可用；
     * 子身份改由各业务表的 active 记录判定（楼长→building_leaders，
     * 统筹/板块负责人/个体负责人→coordinators/sector_leaders/individual_leaders，
     * 活动组组长→activity_leader_identities，
     * 2026-10-20 负责人标签化、2026-10-21 组长标签化，与 V18 楼长改造同模式）。
     */
    private static final Set<String> RESIDENT_SUB_ROLES = Set.of(
            "building_leader", "coordinator", "sector_leader", "individual_leader",
            "activity_leader");

    private final LeaderIdentityGuard leaderIdentityGuard;

    public RoleGuard(LeaderIdentityGuard leaderIdentityGuard) {
        this.leaderIdentityGuard = leaderIdentityGuard;
    }

    /** JWT role 归一化：历史物业子账号角色视为 property_admin；住户子角色视为 resident */
    public static String normalizeRole(String role) {
        if (role == null) {
            return null;
        }
        if (LEGACY_PROPERTY_ADMIN_ROLES.contains(role)) {
            return "property_admin";
        }
        if (RESIDENT_SUB_ROLES.contains(role)) {
            return "resident";
        }
        return role;
    }

    public void requireAnyRole(String... allowedRoles) {
        String role = normalizeRole(UserContext.currentRole());
        if (role == null) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        boolean matched = Arrays.stream(allowedRoles).anyMatch(role::equals);
        if (!matched) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
    }

    public boolean isPlatformAdmin() {
        return "platform_admin".equals(UserContext.currentRole());
    }

    public boolean isPropertyAdmin() {
        return "property_admin".equals(normalizeRole(UserContext.currentRole()));
    }

    public boolean isResident() {
        return "resident".equals(normalizeRole(UserContext.currentRole()));
    }

    public boolean isMerchant() {
        return "merchant".equals(UserContext.currentRole());
    }

    public boolean isCourier() {
        return "courier".equals(UserContext.currentRole());
    }

    public boolean isCoordinator() {
        // 2026-10-20 负责人标签化：身份改由 coordinators 表 active 记录判定（兼容存量 JWT role 值）
        return leaderIdentityGuard.isCurrentCoordinator();
    }

    /**
     * 解析列表查询的物业范围：platform_admin 可跨物业，property_admin 限定本物业，
     * coordinator 为只读下级角色，仅可查看本物业（2026-09-07 前端问题 C）。
     */
    public String resolveListPropertyCompanyId(String requestedPropertyCompanyId) {
        requireAnyRoleOrCoordinatorIdentity("property_admin", "platform_admin", "coordinator");
        if (isPlatformAdmin()) {
            return requestedPropertyCompanyId;
        }
        if (isCoordinator()
                && StringUtils.hasText(requestedPropertyCompanyId)
                && !requestedPropertyCompanyId.equals(UserContext.currentPropertyCompanyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权查看其他物业公司数据");
        }
        return UserContext.currentPropertyCompanyId();
    }

    /**
     * 混合鉴权门（2026-10-20 负责人标签化）：允许名单内角色（管理员等）通过；
     * 或「住户 + 任意一种启用中的负责人身份（统筹/板块/个体）」通过。
     * 用于原 {@code requireAnyRole(..., "coordinator", "sector_leader", "individual_leader")}
     * 的混合门改造——负责人角色值已归一化为 resident，身份按业务表 active 记录判定。
     */
    public void requireAnyRoleOrLeaderIdentity(String... allowedRoles) {
        if (matchesRole(allowedRoles)) {
            return;
        }
        if ("resident".equals(normalizeRole(UserContext.currentRole()))
                && leaderIdentityGuard.hasAnyLeaderIdentity(
                        UserContext.currentUserId(), UserContext.currentPropertyCompanyId())) {
            return;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    /**
     * 混合鉴权门（2026-10-20 负责人标签化）：仅放行「统筹」这一种负责人身份
     * （原门中负责人角色只含 coordinator 时使用，严格保持原有行为，不放行板块/个体负责人）。
     */
    public void requireAnyRoleOrCoordinatorIdentity(String... allowedRoles) {
        if (matchesRole(allowedRoles)) {
            return;
        }
        if ("resident".equals(normalizeRole(UserContext.currentRole()))
                && leaderIdentityGuard.isCurrentCoordinator()) {
            return;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private boolean matchesRole(String... allowedRoles) {
        String role = normalizeRole(UserContext.currentRole());
        return role != null && Arrays.stream(allowedRoles).anyMatch(role::equals);
    }

    public void assertSamePropertyOrPlatform(String targetPropertyCompanyId) {
        if (isPlatformAdmin()) {
            return;
        }
        String currentPropertyCompanyId = UserContext.currentPropertyCompanyId();
        if (currentPropertyCompanyId == null
                || targetPropertyCompanyId == null
                || !currentPropertyCompanyId.equals(targetPropertyCompanyId)) {
            throw new BizException(ErrorCode.RESIDENT_NOT_FOUND);
        }
    }

    public void assertSelfOrAdmin(String targetResidentId) {
        String currentUserId = UserContext.currentUserId();
        if (currentUserId != null && currentUserId.equals(targetResidentId)) {
            return;
        }
        if (isPlatformAdmin() || isPropertyAdmin()) {
            return;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    public static final Set<String> RESIDENT_STATUS = Set.of("active", "frozen", "disabled");
    public static final Set<String> MARITAL_STATUS = Set.of("single", "married", "divorced", "widowed");
    public static final Set<String> FAMILY_RELATIONS = Set.of(
            "owner", "spouse", "child", "parent", "sibling", "other");
    public static final Set<String> FAMILY_AUDIT_STATUS = Set.of("pending", "approved", "rejected");
    public static final Set<String> FAMILY_STATUS = Set.of("active", "inactive");

    public static final Set<String> POINT_TYPES = Set.of(
            "earn", "spend", "transfer", "expire", "pool_in");
    public static final Set<String> POINT_SOURCES = Set.of(
            "order", "activity", "sign_in", "manual", "merchant_grant",
            "fee_deduct", "pool_diff", "transfer", "transfer_to_property");
    public static final Set<String> FEE_PAYMENT_STATUS = Set.of(
            "unpaid", "partial", "paid", "overdue");
    public static final Set<String> FEE_TYPES = Set.of(
            "property", "parking", "water", "electricity");
    public static final Set<String> COIN_TYPES = Set.of(
            "earn", "spend", "recharge", "withdraw", "expire", "freeze", "unfreeze");
    public static final Set<String> COIN_SOURCES = Set.of(
            "order", "property_fee", "manual", "coin_expired", "transfer_to_property");

    public static final int FAMILY_MEMBER_MAX = 15;

    public static final Set<String> MERCHANT_LEVELS = Set.of(
            "official_certified", "property_certified", "normal");
    public static final Set<String> MERCHANT_CATEGORIES = Set.of(
            "外卖", "周围商家", "自营", "农家", "外卖小吃", "团购",
            "美食", "酒店民宿", "洗浴汗蒸", "按摩足疗", "看病买药", "外卖小区");
    public static final Set<String> MERCHANT_AUDIT_STATUS = Set.of(
            "pending_audit", "approved", "rejected");
    public static final Set<String> MERCHANT_OPERATION_STATUS = Set.of("active", "frozen");
    public static final Set<String> PRODUCT_STATUS = Set.of("active", "inactive", "sold_out");
    public static final Set<String> ORDER_STATUS = Set.of(
            "pending", "paid", "delivering", "completed", "cancelled",
            "refunding", "refunded", "refund_rejected");
    public static final Set<String> PAYMENT_METHODS = Set.of(
            "property_coin", "point", "wechat", "mixed");
    /**
     * 配送单状态合法值（含内部态与历史兼容值）。
     *
     * <ul>
     *   <li>{@code merchant_self} / {@code merchant_delivering}：商家自配内部态（骑手不可见）</li>
     *   <li>{@code delivered}：历史部署遗留完成态，读写均归一为 {@code completed}（见 DisplayLabelUtil#normalizeDeliveryStatus）</li>
     * </ul>
     */
    public static final Set<String> DELIVERY_STATUS = Set.of(
            "pending", "accepted", "delivering", "completed", "failed", "property_fallback",
            "merchant_self", "merchant_delivering", "delivered");
    public static final Set<String> DELIVERY_RULE_STATUS = Set.of("active", "inactive");
    public static final Set<String> ANNOUNCEMENT_TYPES = Set.of(
            "property", "community", "platform", "system", "merchant", "activity");
    public static final Set<String> ANNOUNCEMENT_STATUS = Set.of("draft", "published", "archived");
    public static final Set<String> OPINION_BOX_STATUS = Set.of("active", "ended", "closed");
    public static final Set<String> VOTE_OPTIONS = Set.of("support", "oppose", "abstain");
    public static final Set<String> ACTIVITY_GROUP_STATUS = Set.of("active", "inactive");
    public static final Set<String> ACTIVITY_MEMBER_STATUS = Set.of("active", "inactive");
    public static final Set<String> ACTIVITY_SUBSCRIPTION_STATUS = Set.of("active", "expired", "cancelled");
    public static final Set<String> ACTIVITY_GROUP_ROLES = Set.of("leader", "member");
    public static final Set<String> SUBSCRIPTION_TYPES = Set.of("monthly", "quarterly", "yearly", "per_time");
    public static final Set<String> MESSAGE_TYPES = Set.of(
            "property_message", "platform_message", "system_message",
            "community_message", "resident_message", "technician_message",
            "official_message", "service_request", "merchant_ad");
    public static final Set<String> CHAT_TYPES = Set.of("private", "group", "broadcast", "official");
    public static final Set<String> READ_STATUS = Set.of("read", "unread");
    public static final Set<String> BROADCAST_TARGET_ROLES = Set.of(
            "resident", "property_admin", "technician", "courier", "merchant");
    public static final Set<String> FILE_CATEGORIES = Set.of(
            "avatar", "announcement", "merchant", "service", "activity", "product");
    public static final Set<String> SERVICE_CATEGORIES = Set.of(
            "cleaning_repair", "canteen", "tutoring", "elderly_care", "medical",
            "pet_medical", "life_service", "dating");
    public static final Set<String> SERVICE_PROVIDER_TYPES = Set.of(
            "merchant", "resident", "individual_leader");
    public static final Set<String> COMMUNITY_SERVICE_STATUS = Set.of("active", "inactive");
    public static final Set<String> SERVICE_ORDER_STATUS = Set.of(
            "pending", "confirmed", "in_progress", "completed", "cancelled");
    public static final Set<String> PROPERTY_COMPANY_STATUS = Set.of("active", "inactive");
    public static final Set<String> COIN_ISSUE_MODES = Set.of("auto", "manual");

    /**
     * 可被管理端直接写入 residents.role 的角色白名单。
     * 注意：不含 building_leader / coordinator / sector_leader / individual_leader /
     * activity_leader —— 楼长、三类负责人、活动组组长都是住户的叠加身份
     * （由各自业务表判定），不允许通过改 role 的方式授予，否则会破坏该住户的住户功能
     * （社区论坛等）。
     * （2026-10-20 负责人标签化：coordinator/sector_leader/individual_leader 移出白名单；
     *  2026-10-21 组长标签化：activity_leader 移出白名单，改走任命/申请流程）
     */
    public static final Set<String> VALID_ROLES = Set.of(
            "platform_admin", "property_admin", "community_admin",
            "resident", "merchant", "courier", "technician");

    public static final Set<String> VALID_COMMUNITY_SERVICE_CATEGORIES = Set.of(
            "cleaning_repair", "canteen", "tutoring", "elderly_care", "medical",
            "pet_medical", "life_service", "dating");

    public boolean isCommunityAdmin() {
        return "community_admin".equals(UserContext.currentRole());
    }

    public boolean isActivityLeader() {
        // 2026-10-21 组长标签化：身份改由 activity_leader_identities 表 active 记录判定
        // （兼容存量 JWT role=activity_leader）
        return leaderIdentityGuard.isCurrentActivityLeader();
    }

    /**
     * 混合鉴权门（2026-10-21 组长标签化）：允许名单内角色（merchant/technician 等）通过；
     * 或「住户 + 启用中的活动组组长身份」通过。
     * 用于原 {@code requireAnyRole(..., "activity_leader")} 中不含 resident 的门
     * （如商家端商品管理、组长公告发布）——组长角色值已归一化为 resident，身份按业务表判定。
     */
    public void requireAnyRoleOrActivityLeaderIdentity(String... allowedRoles) {
        if (matchesRole(allowedRoles)) {
            return;
        }
        if ("resident".equals(normalizeRole(UserContext.currentRole()))
                && leaderIdentityGuard.isCurrentActivityLeader()) {
            return;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    public boolean isSectorLeader() {
        // 2026-10-20 负责人标签化：身份改由 sector_leaders 表 active 记录判定（兼容存量 JWT role 值）
        return leaderIdentityGuard.isCurrentSectorLeader();
    }

    public boolean isIndividualLeader() {
        // 2026-10-20 负责人标签化：身份改由 individual_leaders 表 active 记录判定（兼容存量 JWT role 值）
        return leaderIdentityGuard.isCurrentIndividualLeader();
    }

    /**
     * 是否为"居民衍生的组长类角色"：业主、活动组长、协调员、板块负责人、个体负责人、单元楼长。
     * 这些角色均以居民账户（res_xxx）存在，可作为活动组负责人，按 leaderId 归属校验。
     * 单元楼长已是住户子角色（role=resident），「是否楼长」请查 building_leaders 表的 active 记录。
     */
    public boolean isLeaderRole() {
        String role = UserContext.currentRole();
        return "resident".equals(role)
                || "activity_leader".equals(role)
                || "coordinator".equals(role)
                || "sector_leader".equals(role)
                || "individual_leader".equals(role)
                || "building_leader".equals(role);
    }
}
