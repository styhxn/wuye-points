package com.wuye.points.interceptor;

import com.wuye.points.context.UserContext;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.RoleGuard;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.Set;

/**
 * 权限拦截器：根据路径和角色校验权限码
 * 对管理端 /admin/** 路径自动校验权限
 */
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final PermissionGuard permissionGuard;
    private final RoleGuard roleGuard;

    private static final Map<String, String> ADMIN_PATH_PERMISSIONS = Map.ofEntries(
            // 平台商家库
            Map.entry("GET:/admin/platform-merchants", "merchant:read"),
            Map.entry("POST:/admin/platform-merchants", "merchant:create"),
            Map.entry("PUT:/admin/platform-merchants", "merchant:update"),
            Map.entry("POST:/admin/platform-merchants/batch-config", "merchant:update"),
            // 商家挂接审核
            Map.entry("POST:/admin/merchant-audit", "merchant:audit"),
            Map.entry("GET:/admin/merchant-point-purchases", "merchant:audit"),
            Map.entry("POST:/admin/merchant-point-purchases", "merchant:audit"),
            Map.entry("GET:/admin/merchant-withdrawals", "merchant:audit"),
            Map.entry("POST:/admin/merchant-withdrawals", "merchant:audit"),
            // 物业公司管理
            Map.entry("GET:/admin/property-companies", "property_company:read"),
            Map.entry("POST:/admin/property-companies", "property_company:create"),
            Map.entry("PUT:/admin/property-companies", "property_company:update"),
            Map.entry("PATCH:/admin/property-companies", "property_company:update"),
            // 权限管理
            Map.entry("GET:/admin/permissions", "permission:read"),
            Map.entry("POST:/admin/role-presets", "permission:manage"),
            Map.entry("PUT:/admin/role-presets", "permission:manage"),
            Map.entry("GET:/admin/role-presets", "permission:read"),
            Map.entry("GET:/admin/users", "permission:read"),
            Map.entry("POST:/admin/users", "permission:manage"),
            Map.entry("GET:/admin/users/*/permissions", "permission:read"),
            Map.entry("POST:/admin/users/*/permissions/grant", "permission:manage"),
            Map.entry("POST:/admin/users/*/permissions/revoke", "permission:manage"),
            Map.entry("GET:/admin/permission-change-logs", "permission:read"),
            // 统筹管理
            Map.entry("GET:/admin/coordinators", "coordinator:read"),
            Map.entry("POST:/admin/coordinators", "coordinator:manage"),
            Map.entry("PUT:/admin/coordinators", "coordinator:manage"),
            Map.entry("DELETE:/admin/coordinators", "coordinator:manage"),
            // 板块负责人
            Map.entry("GET:/admin/sector-leaders", "sector:read"),
            Map.entry("POST:/admin/sector-leaders", "sector:manage"),
            Map.entry("PUT:/admin/sector-leaders", "sector:manage"),
            Map.entry("DELETE:/admin/sector-leaders", "sector:manage"),
            // 个体负责人
            Map.entry("GET:/admin/individual-leaders", "individual:read"),
            Map.entry("POST:/admin/individual-leaders", "individual:manage"),
            // 积分池
            Map.entry("GET:/admin/point-pools", "pool:read"),
            Map.entry("POST:/admin/point-pools", "pool:adjust"),
            Map.entry("GET:/admin/point-pools/records", "pool:read"),
            // 线下积分
            Map.entry("POST:/admin/offline-points", "points:input"),
            Map.entry("GET:/admin/offline-points/records", "points:input"),
            // 报表
            Map.entry("GET:/admin/reports", "report:view"),
            // 操作日志
            Map.entry("GET:/admin/operation-logs", "log:read"),
            // 物业币管控
            Map.entry("GET:/admin/coin-freeze-records", "coin:freeze"),
            Map.entry("POST:/admin/coin-withdrawals", "coin:freeze"),
            Map.entry("GET:/admin/coin-withdrawals", "coin:freeze"),
            Map.entry("POST:/admin/coin-expiry", "coin:expiry_run"),
            Map.entry("GET:/admin/coin-expiry/records", "coin:expiry_run"),
            // 2年清零
            Map.entry("POST:/admin/point-clear", "point:clear_run"),
            Map.entry("GET:/admin/point-clear/records", "point:clear_run"),
            // 商家踢出
            Map.entry("POST:/admin/merchants/*/kick", "merchant:kick"),
            // 公告管理
            Map.entry("GET:/admin/announcements", "announcement:read")
    );

    private static final Set<String> PLATFORM_ONLY_PATHS = Set.of(
            "/admin/property-companies"   // 创建/列表仅 platform_admin
    );

    /**
     * 2026-09-08 前端问题：统筹写操作 403。
     * 统筹（coordinator）按文档 §39 总述可管理本人名下板块负责人，此处放行 sector:manage；
     * 数据范围（本物业 / 本人名下）由 AdminSectorLeaderService 二次收敛。
     */
    private static final Set<String> COORDINATOR_WRITABLE_CODES = Set.of("sector:manage");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        if (!uri.contains("/admin/")) {
            return true;
        }

        String role = UserContext.currentRole();
        if (role == null) {
            return true;
        }

        if ("platform_admin".equals(role)) {
            return true;
        }

        String method = request.getMethod();
        String basePath = extractAdminPath(uri);
        String key = method + ":" + basePath;

        String permissionCode = ADMIN_PATH_PERMISSIONS.get(key);
        if (permissionCode != null) {
            // 2026-09-07 前端问题 C：coordinator 持有只读权限码（如 sector:read）时放行 GET 查询；
            // 2026-09-08 前端问题：sector:manage 等白名单写权限码同样按权限码放行，其余写操作仍走 requireAdmin。
            if ("coordinator".equals(role)
                    && (permissionCode.endsWith(":read") || COORDINATOR_WRITABLE_CODES.contains(permissionCode))) {
                permissionGuard.requirePermissionOrAdminAllowingCoordinator(permissionCode);
            } else {
                permissionGuard.requirePermission(permissionCode);
            }
        }

        return true;
    }

    private String extractAdminPath(String uri) {
        int adminIndex = uri.indexOf("/admin/");
        if (adminIndex < 0) {
            return uri;
        }
        // 获取 /admin/ 之后的所有路径
        String fullPath = uri.substring(adminIndex);
        // 将路径参数（纯数字）替换为 *
        String[] segments = fullPath.split("/");
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
