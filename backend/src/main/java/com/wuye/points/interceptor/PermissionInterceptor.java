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
            // 商家挂接审核
            Map.entry("POST:/admin/merchant-audit", "merchant:audit"),
            // 物业公司管理
            Map.entry("GET:/admin/property-companies", "property_company:read"),
            Map.entry("POST:/admin/property-companies", "property_company:create"),
            Map.entry("PUT:/admin/property-companies", "property_company:update"),
            Map.entry("PATCH:/admin/property-companies", "property_company:update"),
            // 权限管理
            Map.entry("GET:/admin/permissions", "permission:read"),
            Map.entry("POST:/admin/role-presets", "permission:manage"),
            Map.entry("PUT:/admin/role-presets", "permission:manage"),
            Map.entry("GET:/admin/users", "permission:read"),
            Map.entry("POST:/admin/users", "permission:manage"),
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
            // 线下积分
            Map.entry("POST:/admin/offline-points", "points:input"),
            // 报表
            Map.entry("GET:/admin/reports", "report:view"),
            // 操作日志
            Map.entry("GET:/admin/operation-logs", "log:read"),
            // 物业币管控
            Map.entry("POST:/admin/residents", "coin:freeze"),
            Map.entry("POST:/admin/coin-expiry", "coin:expiry_run"),
            // 2年清零
            Map.entry("POST:/admin/point-clear", "point:clear_run")
    );

    private static final Set<String> PLATFORM_ONLY_PATHS = Set.of(
            "/admin/property-companies"   // 创建/列表仅 platform_admin
    );

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
            permissionGuard.requirePermission(permissionCode);
        }

        return true;
    }

    private String extractAdminPath(String uri) {
        int adminIndex = uri.indexOf("/admin/");
        if (adminIndex < 0) {
            return uri;
        }
        String path = uri.substring(adminIndex + "/admin".length());
        String[] segments = path.split("/");
        if (segments.length >= 2) {
            return "/" + segments[1];
        }
        return path;
    }
}
