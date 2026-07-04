package com.wuye.points.util;

import com.wuye.points.context.UserContext;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

/**
 * 角色与数据范围校验
 */
@Component
public class RoleGuard {

    public void requireAnyRole(String... allowedRoles) {
        String role = UserContext.currentRole();
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
        return "property_admin".equals(UserContext.currentRole());
    }

    public boolean isResident() {
        return "resident".equals(UserContext.currentRole());
    }

    public boolean isMerchant() {
        return "merchant".equals(UserContext.currentRole());
    }

    public boolean isCourier() {
        return "courier".equals(UserContext.currentRole());
    }

    public boolean isCoordinator() {
        return "coordinator".equals(UserContext.currentRole());
    }

    /**
     * 解析列表查询的物业范围：platform_admin 可跨物业，property_admin 限定本物业
     */
    public String resolveListPropertyCompanyId(String requestedPropertyCompanyId) {
        requireAnyRole("property_admin", "platform_admin");
        if (isPlatformAdmin()) {
            return requestedPropertyCompanyId;
        }
        return UserContext.currentPropertyCompanyId();
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
            "fee_deduct", "pool_diff", "transfer");
    public static final Set<String> FEE_PAYMENT_STATUS = Set.of(
            "unpaid", "partial", "paid", "overdue");
    public static final Set<String> FEE_TYPES = Set.of(
            "property", "parking", "water", "electricity");
    public static final Set<String> COIN_TYPES = Set.of(
            "earn", "spend", "recharge", "withdraw", "expire", "freeze", "unfreeze");
    public static final Set<String> COIN_SOURCES = Set.of(
            "order", "property_fee", "manual", "coin_expired");

    public static final int FAMILY_MEMBER_MAX = 15;

    public static final Set<String> MERCHANT_LEVELS = Set.of(
            "official_certified", "property_certified", "normal");
    public static final Set<String> MERCHANT_CATEGORIES = Set.of(
            "外卖", "周围商家", "自营", "农家", "外卖小吃", "团购");
    public static final Set<String> MERCHANT_AUDIT_STATUS = Set.of(
            "pending_audit", "approved", "rejected");
    public static final Set<String> MERCHANT_OPERATION_STATUS = Set.of("active", "frozen");
    public static final Set<String> PRODUCT_STATUS = Set.of("active", "inactive", "sold_out");
    public static final Set<String> ORDER_STATUS = Set.of(
            "pending", "paid", "delivering", "completed", "cancelled", "refunding", "refunded");
    public static final Set<String> PAYMENT_METHODS = Set.of(
            "property_coin", "point", "wechat", "mixed");
    public static final Set<String> DELIVERY_STATUS = Set.of(
            "pending", "accepted", "delivering", "completed", "failed", "property_fallback");
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
    public static final Set<String> SUBSCRIPTION_TYPES = Set.of("monthly", "yearly");
    public static final Set<String> MESSAGE_TYPES = Set.of(
            "property_message", "platform_message", "system_message",
            "community_message", "resident_message", "technician_message");
    public static final Set<String> CHAT_TYPES = Set.of("private", "group", "broadcast");
    public static final Set<String> READ_STATUS = Set.of("read", "unread");
    public static final Set<String> BROADCAST_TARGET_ROLES = Set.of(
            "resident", "property_admin", "technician", "courier", "merchant");
    public static final Set<String> FILE_CATEGORIES = Set.of(
            "avatar", "announcement", "merchant", "service", "activity");
    public static final Set<String> SERVICE_CATEGORIES = Set.of(
            "cleaning_repair", "canteen", "tutoring", "elderly_care", "medical",
            "pet_medical", "life_service", "dating");
    public static final Set<String> SERVICE_PROVIDER_TYPES = Set.of(
            "merchant", "resident", "individual_leader");
    public static final Set<String> COMMUNITY_SERVICE_STATUS = Set.of("active", "inactive");
    public static final Set<String> SERVICE_ORDER_STATUS = Set.of(
            "pending", "confirmed", "in_progress", "completed", "cancelled");
    public static final Set<String> PROPERTY_COMPANY_STATUS = Set.of("active", "inactive");
    public static final Set<String> COIN_ISSUE_MODES = Set.of("auto", "manual_approval");

    public static final Set<String> VALID_ROLES = Set.of(
            "platform_admin", "property_admin", "community_admin",
            "resident", "merchant", "courier", "technician",
            "coordinator", "sector_leader", "individual_leader",
            "activity_leader");

    public static final Set<String> VALID_COMMUNITY_SERVICE_CATEGORIES = Set.of(
            "cleaning_repair", "canteen", "tutoring", "elderly_care", "medical",
            "pet_medical", "life_service", "dating");

    public boolean isCommunityAdmin() {
        return "community_admin".equals(UserContext.currentRole());
    }
}
