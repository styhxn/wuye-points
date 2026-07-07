package com.wuye.points.interceptor;

import com.wuye.points.context.UserContext;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器：解析 JWT 并注入 UserContext
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (isOptionalAuth(request)) {
                return true;
            }
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }

        String token = authHeader.substring(7);
        Claims claims = jwtUtil.parseToken(token);
        jwtUtil.validateAccessToken(claims);
        UserContext context = UserContext.builder()
                .userId(claims.getSubject())
                .role(claims.get("role", String.class))
                .propertyCompanyId(claims.get("propertyCompanyId", String.class))
                .familyId(claims.get("familyId", String.class))
                .build();
        UserContext.set(context);
        return true;
    }

    /**
     * GET /merchants、GET /merchants/:id、GET /products、GET /products/:id、GET /announcements 等允许未登录浏览
     */
    private boolean isOptionalAuth(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        if (uri == null) {
            return false;
        }

        // 提取相对路径
        String path = extractRelativePath(uri);

        return isOptionalMerchantPath(path)
                || isOptionalProductPath(path)
                || isOptionalPublicPath(path);
    }

    private String extractRelativePath(String uri) {
        String[] prefixes = {"/api/v1", "/api"};
        for (String prefix : prefixes) {
            if (uri.startsWith(prefix)) {
                return uri.substring(prefix.length());
            }
        }
        return uri;
    }

    private boolean isOptionalMerchantPath(String path) {
        if ("/merchants".equals(path)) {
            return true;
        }
        if (path.startsWith("/merchants/")) {
            String suffix = path.substring("/merchants/".length());
            return !suffix.isEmpty() && !"my".equals(suffix) && !suffix.contains("/");
        }
        return false;
    }

    private boolean isOptionalProductPath(String path) {
        if ("/products".equals(path)) {
            return true;
        }
        if (path.startsWith("/products/")) {
            String suffix = path.substring("/products/".length());
            return !suffix.isEmpty() && !suffix.contains("/");
        }
        return false;
    }

    private boolean isOptionalPublicPath(String path) {
        // 精确匹配路径
        if ("/announcements".equals(path) || "/services".equals(path)
                || "/activity-groups".equals(path) || "/special-offers".equals(path)
                || "/property-companies".equals(path)) {
            return true;
        }
        // 匹配 /{resource}/{id} 模式（排除子路径）
        if (path.startsWith("/announcements/") || path.startsWith("/services/")
                || path.startsWith("/activity-groups/") || path.startsWith("/special-offers/")
                || path.startsWith("/property-companies/")) {
            String suffix = path.substring(path.indexOf('/', 1) + 1);
            return !suffix.contains("/");
        }
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}
