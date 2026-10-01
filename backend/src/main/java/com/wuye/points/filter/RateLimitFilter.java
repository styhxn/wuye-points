package com.wuye.points.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter implements Filter {

    @Value("${app.rate-limit.max-attempts:10}")
    private int maxAttempts;

    @Value("${app.rate-limit.window-ms:300000}")
    private long windowMs;

    private final Map<String, RateLimitEntry> attempts = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // CORS 预检请求不计入限流，否则跨域场景下配额会被凭空消耗一半
        if (HttpMethod.OPTIONS.matches(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String key = resolveKey(httpRequest);
        if (key != null && isRateLimited(key)) {
            writeCorsHeaders(httpRequest, httpResponse);
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"code\":429001,\"message\":\"请求过于频繁，请稍后再试\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    /**
     * 该过滤器在 Spring MVC 之前直接写出响应，需要自行补齐跨域响应头，
     * 否则前端只能看到含糊的 CORS 报错而看不到真实的 429 提示
     */
    private void writeCorsHeaders(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Vary", "Origin");
        }
    }

    private String resolveKey(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.contains("/auth/admin-login")) {
            return "admin_login:" + getClientIp(request);
        }
        if (uri.contains("/auth/wechat-login")) {
            return "wechat_login:" + getClientIp(request);
        }
        return null;
    }

    private boolean isRateLimited(String key) {
        long now = System.currentTimeMillis();
        RateLimitEntry entry = attempts.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart > windowMs) {
                return new RateLimitEntry(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });
        return entry.count.get() > maxAttempts;
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

    private static class RateLimitEntry {
        final long windowStart;
        final AtomicInteger count;

        RateLimitEntry(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
