package com.wuye.points.interceptor;

import com.wuye.points.context.UserContext;
import com.wuye.points.util.AdminAuthorizationContext;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.RoleGuard;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 权限拦截器：把管理端路由映射到权限码，并交给 {@link PermissionGuard} 校验。
 *
 * <p>映射优先级：精确路由（{@link #ADMIN_PATH_PERMISSIONS}）→ 模块前缀规则
 * （{@link #MODULE_RULES}，最长前缀优先）。GET/HEAD 走模块的读权限码，
 * 其余方法走写权限码；OPTIONS 直接放行。</p>
 *
 * <p>另有一组“管理端使用的非 /admin 路径”（公告发布、报表），
 * 仅在调用方具备管理端工作台身份时按权限码校验，避免影响住户/商家移动端。
 * 移动端共用的商家申请/店铺维护接口不在其中。</p>
 */
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final PermissionGuard permissionGuard;
    private final RoleGuard roleGuard;

    /** 模块前缀规则：读方法使用 readCode，写方法使用 manageCode。 */
    private record ModuleRule(String prefix, String readCode, String manageCode) {
    }

    private static final Map<String, String> ADMIN_PATH_PERMISSIONS = Map.ofEntries(
            // 平台商家库
            Map.entry("GET:/admin/platform-merchants", "merchant:read"),
            Map.entry("POST:/admin/platform-merchants", "merchant:create"),
            Map.entry("GET:/admin/platform-merchants/*", "merchant:read"),
            Map.entry("PUT:/admin/platform-merchants/*", "merchant:update"),
            Map.entry("POST:/admin/platform-merchants/batch-config", "merchant:update"),
            Map.entry("POST:/admin/platform-merchants/*/link", "merchant:audit"),
            Map.entry("POST:/admin/platform-merchants/*/recommend", "rank:drag"),
            // 商家挂接审核
            Map.entry("POST:/admin/merchant-audit", "merchant:audit"),
            Map.entry("POST:/admin/merchants/*/kick", "merchant:kickout"),
            Map.entry("POST:/admin/merchants/*/recommend", "rank:drag"),
            Map.entry("PUT:/admin/merchants/*/recommend", "rank:drag"),
            Map.entry("GET:/admin/merchants/*/ad-quota", "promotion:push"),
            Map.entry("POST:/admin/merchants/*/ad-quota", "promotion:push"),
            Map.entry("GET:/admin/merchants/*/community-distances", "config:set"),
            Map.entry("PUT:/admin/merchants/*/community-distances", "config:set"),
            Map.entry("GET:/admin/merchants/*/wechat-account", "config:set"),
            Map.entry("PUT:/admin/merchants/*/wechat-account", "config:set"),
            // 物业公司管理（platform-only 码，property_admin 依赖未入库码的旧回退，行为保持不变）
            Map.entry("GET:/admin/property-companies", "property_company:read"),
            Map.entry("POST:/admin/property-companies", "property_company:create"),
            Map.entry("PUT:/admin/property-companies", "property_company:update"),
            Map.entry("PATCH:/admin/property-companies", "property_company:update"),
            // 参数配置写入口：统一按 config:set 校验（物业领导/平台）；物业操作员不可写
            Map.entry("PATCH:/admin/property-companies/*/config", "config:set"),
            Map.entry("PUT:/admin/property-companies/*/coin-use-condition", "config:set"),
            Map.entry("PUT:/admin/property-companies/*/coin-display", "coin:display_control"),
            // 平台专属的物业公司动作：重置密码、删除、结算提现审批
            Map.entry("POST:/admin/property-companies/*/admin-accounts/*/reset-password", "property_company:manage"),
            Map.entry("DELETE:/admin/property-companies/*", "property_company:manage"),
            Map.entry("POST:/admin/property-companies/*/settlement-withdrawals/*/approve", "property_company:manage"),
            Map.entry("POST:/admin/property-companies/*/settlement-withdrawals/*/reject", "property_company:manage"),
            // 权限管理
            Map.entry("GET:/admin/permissions", "permission:read"),
            Map.entry("GET:/admin/role-presets", "permission:read"),
            Map.entry("POST:/admin/role-presets", "permission:manage"),
            Map.entry("PUT:/admin/role-presets", "permission:manage"),
            Map.entry("PUT:/admin/role-presets/*", "permission:manage"),
            Map.entry("GET:/admin/users", "permission:read"),
            Map.entry("POST:/admin/users", "permission:manage"),
            Map.entry("GET:/admin/users/*/permissions", "permission:read"),
            Map.entry("POST:/admin/users/*/permissions/grant", "permission:manage"),
            Map.entry("POST:/admin/users/*/permissions/revoke", "permission:manage"),
            Map.entry("GET:/admin/permission-change-logs", "permission:read"),
            // 积分池（POST 调整与查询分开）
            Map.entry("POST:/admin/point-pools/adjust", "pool:adjust"),
            Map.entry("GET:/admin/point-pools/records", "pool:read"),
            // 线下积分
            Map.entry("POST:/admin/offline-points/grant", "points:input"),
            Map.entry("GET:/admin/offline-points/records", "points:input"),
            // 社区论坛
            Map.entry("GET:/admin/community/posts", "community:read"),
            Map.entry("GET:/admin/community/posts/*/comments", "community:read"),
            Map.entry("DELETE:/admin/community/posts/*", "community:manage"),
            Map.entry("DELETE:/admin/community/comments/*", "community:manage"),
            Map.entry("POST:/admin/community/posts/*/visibility", "community:manage"),
            Map.entry("GET:/admin/community/reports", "community:read"),
            Map.entry("POST:/admin/community/reports/*/handle", "community:manage"),
            Map.entry("GET:/admin/community/forum-bans", "community:read"),
            Map.entry("POST:/admin/community/users/*/forum-ban", "community:manage"),
            Map.entry("DELETE:/admin/community/users/*/forum-ban", "community:manage"),
            // 报表 / 操作日志
            Map.entry("GET:/admin/reports", "reports:view"),
            Map.entry("GET:/admin/operation-logs", "log:read"),
            // 物业币管控
            Map.entry("GET:/admin/coin-freeze-records", "coin:freeze"),
            Map.entry("POST:/admin/residents/*/coin/freeze", "coin:freeze"),
            Map.entry("POST:/admin/residents/*/coin/unfreeze", "coin:freeze"),
            Map.entry("PUT:/admin/residents/*/coin/display", "coin:display_control"),
            Map.entry("GET:/admin/coin-withdrawals/settings", "config:set"),
            Map.entry("PUT:/admin/coin-withdrawals/settings", "config:set"),
            Map.entry("PUT:/admin/coin-withdrawals/residents/*/block", "config:set"),
            Map.entry("PUT:/admin/coin-withdrawals/merchants/*/block", "config:set"),
            // 权限池没有“物业币到期清零”码，沿用原 coin:expiry_run：
            // 未入库时回退为物业管理员/平台管理员，标签身份不可用。
            Map.entry("GET:/admin/coin-expiry/records", "coin:expiry_run"),
            Map.entry("POST:/admin/coin-expiry/run", "coin:expiry_run"),
            // 2年清零
            Map.entry("POST:/admin/point-clear/run", "point:clear_run"),
            Map.entry("GET:/admin/point-clear/records", "point:clear_run"),
            // 公告征集名单
            Map.entry("GET:/admin/announcements/*/collects", "notice:collect"),
            // 定向消息统计
            Map.entry("GET:/admin/directed-messages/stats", "directed_message:stats"),
            // 平台专属动作：复用 property_company:manage（platform-only 码），
            // 避免物业管理员/标签身份在权限配置里看到入口却必然 403。
            Map.entry("POST:/admin/company-account/adjust", "property_company:manage"),
            Map.entry("POST:/admin/property-coin-funds/transfers", "property_company:manage"),
            Map.entry("PUT:/admin/platform-share-rates", "property_company:manage"),
            Map.entry("GET:/admin/platform-earnings/stats", "property_company:manage"),
            Map.entry("GET:/admin/platform-earnings/records", "property_company:manage"),
            Map.entry("GET:/admin/platform-earnings/balance", "property_company:manage"),
            Map.entry("GET:/admin/transactions/export", "property_company:manage"),
            Map.entry("GET:/admin/wechat-pay/status", "property_company:manage"),
            Map.entry("GET:/admin/wechat-split/receivers", "property_company:manage"),
            Map.entry("POST:/admin/wechat-split/receivers", "property_company:manage"),
            Map.entry("DELETE:/admin/wechat-split/receivers/*", "property_company:manage")
    );

    /** 模块前缀 → 读写权限码；按前缀长度倒序匹配，保证 /admin/community-entities 先于 /admin/community。 */
    private static final List<ModuleRule> MODULE_RULES = List.of(
            new ModuleRule("/admin/activity-groups", "activity_group:manage", "activity_group:manage"),
            new ModuleRule("/admin/activity-leader-applications", "activity_group:manage", "activity_group:manage"),
            new ModuleRule("/admin/activity-leaders", "activity_group:manage", "activity_group:manage"),
            new ModuleRule("/admin/announcements", "notice:collect", "announcement:publish"),
            new ModuleRule("/admin/building-changes", "user:manage", "user:manage"),
            new ModuleRule("/admin/building-leaders", "user:manage", "user:manage"),
            new ModuleRule("/admin/canteen", "config:set", "config:set"),
            new ModuleRule("/admin/coin-expiry", "coin:expiry_run", "coin:expiry_run"),
            new ModuleRule("/admin/coin-freeze-records", "coin:freeze", "coin:freeze"),
            new ModuleRule("/admin/coin-withdrawals", "coin:audit", "coin:audit"),
            new ModuleRule("/admin/community-entities", "community_entity:manage", "community_entity:manage"),
            new ModuleRule("/admin/community-points", "pool:read", "pool:adjust"),
            new ModuleRule("/admin/community", "community:read", "community:manage"),
            new ModuleRule("/admin/company-account", "pool:read", "pool:adjust"),
            new ModuleRule("/admin/consultants", "consultation:manage", "consultation:manage"),
            new ModuleRule("/admin/consultation-settings", "consultation:manage", "consultation:manage"),
            new ModuleRule("/admin/coordinators", "coordinator:read", "coordinator:manage"),
            new ModuleRule("/admin/coupons", "promotion:push", "promotion:push"),
            new ModuleRule("/admin/couriers", "delivery:grab", "delivery:grab"),
            new ModuleRule("/admin/dashboard", "data:view", "data:view"),
            new ModuleRule("/admin/deliveries", "delivery:grab", "delivery:grab"),
            new ModuleRule("/admin/delivery-orders", "delivery:grab", "delivery:grab"),
            new ModuleRule("/admin/delivery-price-ranges", "delivery:grab", "delivery:grab"),
            new ModuleRule("/admin/delivery-rules", "delivery:grab", "delivery:grab"),
            new ModuleRule("/admin/directed-messages", "directed_message:read", "directed_message:send"),
            new ModuleRule("/admin/age-brackets", "directed_message:read", "directed_message:send"),
            new ModuleRule("/admin/distributor-applications", "distributor:manage", "distributor:manage"),
            new ModuleRule("/admin/distributor-products", "distributor:manage", "distributor:manage"),
            new ModuleRule("/admin/distributors", "distributor:manage", "distributor:manage"),
            new ModuleRule("/admin/distribution-records", "distribution:read", "distribution:read"),
            new ModuleRule("/admin/distribution", "distribution:read", "distribution:read"),
            new ModuleRule("/admin/individual-leader-applications", "individual:read", "individual:manage"),
            new ModuleRule("/admin/individual-leaders", "individual:read", "individual:manage"),
            new ModuleRule("/admin/merchant-ad-packages", "promotion:push", "promotion:push"),
            new ModuleRule("/admin/merchant-ad-settings", "promotion:push", "promotion:push"),
            new ModuleRule("/admin/merchant-audit", "merchant:audit", "merchant:audit"),
            new ModuleRule("/admin/merchant-point-purchases", "merchant:audit", "merchant:audit"),
            new ModuleRule("/admin/merchant-withdrawals", "merchant:audit", "merchant:audit"),
            new ModuleRule("/admin/merchants", "merchant:audit", "merchant:audit"),
            new ModuleRule("/admin/messages", "user:manage", "user:manage"),
            new ModuleRule("/admin/navigation-items", "config:set", "config:set"),
            new ModuleRule("/admin/offline-points", "points:input", "points:input"),
            new ModuleRule("/admin/operation-logs", "log:read", "log:read"),
            new ModuleRule("/admin/order-config", "config:set", "config:set"),
            new ModuleRule("/admin/orders", "refund:approve", "refund:approve"),
            new ModuleRule("/admin/permission-change-logs", "permission:read", "permission:read"),
            new ModuleRule("/admin/permissions", "permission:read", "permission:manage"),
            new ModuleRule("/admin/platform-config", "config:set", "config:set"),
            new ModuleRule("/admin/platform-earnings", "distribution:read", "distribution:read"),
            new ModuleRule("/admin/platform-merchants", "merchant:read", "merchant:update"),
            new ModuleRule("/admin/platform-share-rates", "distribution:read", "distribution:read"),
            new ModuleRule("/admin/point-clear", "point:clear_run", "point:clear_run"),
            new ModuleRule("/admin/point-pools", "pool:read", "pool:adjust"),
            new ModuleRule("/admin/points", "data:view", "data:view"),
            new ModuleRule("/admin/profile-rewards", "config:set", "config:set"),
            new ModuleRule("/admin/property-bank-cards", "config:set", "config:set"),
            new ModuleRule("/admin/property-coin-funds", "pool:read", "pool:adjust"),
            new ModuleRule("/admin/property-coin", "config:set", "config:set"),
            // 子路径（详情/更新/状态/配置/结算）统一走 property_company:read：
            // 该码未入库时回退为“物业管理员或平台管理员”，与改造前的行为一致；标签身份不放行。
            new ModuleRule("/admin/property-companies", "property_company:read", "property_company:read"),
            new ModuleRule("/admin/property-contact", "config:set", "config:set"),
            new ModuleRule("/admin/property-fees", "property_fee:manage", "property_fee:manage"),
            new ModuleRule("/admin/property-managers", "property_operator:manage", "property_operator:manage"),
            new ModuleRule("/admin/property-operators", "property_operator:manage", "property_operator:manage"),
            new ModuleRule("/admin/region-quotas", "config:set", "config:set"),
            new ModuleRule("/admin/resident-merchant-applications", "resident_merchant:audit", "resident_merchant:audit"),
            new ModuleRule("/admin/resident-merchant-deposits", "resident_merchant:audit", "resident_merchant:audit"),
            new ModuleRule("/admin/resident-merchant-settlements", "resident_merchant:audit", "resident_merchant:audit"),
            new ModuleRule("/admin/resident-merchant-settings", "resident_merchant:audit", "resident_merchant:audit"),
            new ModuleRule("/admin/residents", "user:manage", "user:manage"),
            new ModuleRule("/admin/role-accounts", "user:manage", "user:manage"),
            new ModuleRule("/admin/role-presets", "permission:read", "permission:manage"),
            new ModuleRule("/admin/role-withdrawals", "coin:audit", "coin:audit"),
            new ModuleRule("/admin/sector-leaders", "sector:read", "sector:manage"),
            new ModuleRule("/admin/settlement-config", "config:set", "config:set"),
            new ModuleRule("/admin/split-recovery", "distribution:read", "distribution:read"),
            new ModuleRule("/admin/stats", "data:view", "data:view"),
            new ModuleRule("/admin/transactions", "data:view", "data:view"),
            new ModuleRule("/admin/transfer-to-property", "data:view", "data:view"),
            new ModuleRule("/admin/users", "permission:read", "permission:manage"),
            new ModuleRule("/admin/wechat-pay", "property_company:manage", "property_company:manage"),
            new ModuleRule("/admin/wechat-split", "property_company:manage", "property_company:manage")
    ).stream()
            .sorted(Comparator.comparingInt((ModuleRule rule) -> rule.prefix().length()).reversed())
            .toList();

    /**
     * 管理端页面实际调用、但不在 /admin 下的接口。
     * 仅在调用方具备管理端工作台身份时校验，避免影响住户/商家移动端同名接口。
     */
    private static final Map<String, String> ADMIN_OPERATION_PERMISSIONS = Map.ofEntries(
            Map.entry("POST:/announcements", "announcement:publish"),
            Map.entry("PUT:/announcements/*", "announcement:publish"),
            Map.entry("DELETE:/announcements/*", "announcement:publish"),
            // 注意：/merchants POST（住户入驻申请）与 PUT（商家/组长/技工改本人店）是移动端共用接口，
            // 不能按管理端权限码拦截；商家管理走 /admin/merchant-* 与 /admin/merchants/*/audit。
            Map.entry("GET:/reports/overview", "reports:view"),
            Map.entry("GET:/reports/point-summary", "reports:view"),
            Map.entry("GET:/reports/order-summary", "reports:view"),
            Map.entry("GET:/reports/distribution-summary", "reports:view"),
            Map.entry("GET:/reports/export", "reports:view")
    );

    /**
     * 2026-09-08 前端问题：统筹写操作 403。
     * 统筹（coordinator）按文档 §39 总述可管理本人名下板块负责人，此处放行 sector:manage；
     * 数据范围（本物业 / 本人名下）由 AdminSectorLeaderService 二次收敛。
     */
    private static final Set<String> COORDINATOR_WRITABLE_CODES = Set.of("sector:manage");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String role = UserContext.currentRole();
        if (role == null) {
            return true;
        }

        if ("platform_admin".equals(role)) {
            return true;
        }

        String rawPath = resolvePath(request);
        String method = request.getMethod();
        String exactKey = method + ":" + toExactPath(rawPath);

        if (rawPath.startsWith("/admin/")) {
            String permissionCode = ADMIN_PATH_PERMISSIONS.get(exactKey);
            if (permissionCode == null) {
                permissionCode = resolveModulePermission(rawPath, method);
            }
            if (permissionCode != null) {
                authorize(permissionCode);
            }
            return true;
        }

        // 非 /admin 路径只针对管理端工作台身份生效；先查映射再判身份，避免给普通移动端请求增加身份查询。
        String operationCode = ADMIN_OPERATION_PERMISSIONS.get(exactKey);
        if (operationCode != null && roleGuard.isAdminWorkspaceUser()) {
            // 活动组长在移动端编辑/下架自己发布的公告，Service 已按发布人收敛；
            // 这里不要求 announcement.publish，避免误伤组长移动端功能。
            if (isActivityLeaderAnnouncementWrite(exactKey) && roleGuard.isActivityLeader()) {
                return true;
            }
            authorize(operationCode);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        AdminAuthorizationContext.clear();
    }

    private void authorize(String permissionCode) {
        // 2026-09-07 前端问题 C：coordinator 持有只读权限码（如 sector:read）时放行 GET 查询；
        // 2026-09-08 前端问题：sector:manage 等白名单写权限码同样按权限码放行，其余写操作仍走 requirePermission。
        // 2026-10-20 负责人标签化：coordinator 身份改由 coordinators 表 active 记录判定（兼容存量 JWT role 值）。
        if (roleGuard.isCoordinator()
                && (permissionCode.endsWith(":read") || COORDINATOR_WRITABLE_CODES.contains(permissionCode))) {
            permissionGuard.requirePermissionOrAdminAllowingCoordinator(permissionCode);
        } else {
            permissionGuard.requirePermission(permissionCode);
        }
        // 业务层历史角色门据此放行已按权限码授权的标签身份。
        AdminAuthorizationContext.markGranted();
    }

    private String resolveModulePermission(String rawPath, String method) {
        for (ModuleRule rule : MODULE_RULES) {
            if (rawPath.equals(rule.prefix()) || rawPath.startsWith(rule.prefix() + "/")) {
                return isReadMethod(method) ? rule.readCode() : rule.manageCode();
            }
        }
        return null;
    }

    private boolean isReadMethod(String method) {
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method);
    }

    private boolean isActivityLeaderAnnouncementWrite(String exactKey) {
        return "PUT:/announcements/*".equals(exactKey) || "DELETE:/announcements/*".equals(exactKey);
    }

    /** 去掉 context-path / /api/v1 前缀和尾部斜杠，得到控制器路由路径。 */
    private String resolvePath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        if (path.startsWith("/api/v1/")) {
            path = path.substring("/api/v1".length());
        }
        int adminIndex = path.indexOf("/admin/");
        if (adminIndex > 0) {
            path = path.substring(adminIndex);
        }
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path.isEmpty() ? "/" : path;
    }

    /** 精确路由占位符：数字、res_xxx / m_xxx / pc_xxx 等业务主键段统一替换为 *。 */
    private String toExactPath(String rawPath) {
        String[] segments = rawPath.split("/");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) {
                result.append("/");
            }
            if (segments[i].matches("\\d+") || segments[i].matches("[a-z]+_[a-zA-Z0-9]+")) {
                result.append("*");
            } else {
                result.append(segments[i]);
            }
        }
        return result.toString();
    }
}
