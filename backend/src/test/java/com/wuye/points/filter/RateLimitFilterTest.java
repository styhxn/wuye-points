package com.wuye.points.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 登录限流回归：小程序登录与管理端登录分档配额，并把剩余次数回写给前端。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RateLimitFilter 登录分档配额与响应头")
class RateLimitFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() throws Exception {
        filter = new RateLimitFilter();
        ReflectionTestUtils.setField(filter, "maxAttempts", 10);
        ReflectionTestUtils.setField(filter, "wechatMaxAttempts", 30);
        ReflectionTestUtils.setField(filter, "windowMs", 300_000L);
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.7");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    }

    @Test
    @DisplayName("小程序登录放宽到 30 次：第 30 次放行并回写剩余次数，第 31 次才 429")
    void wechatLoginAllowsThirtyAttempts() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/wechat-login");

        for (int i = 1; i <= 30; i++) {
            filter.doFilter(request, response, chain);
        }
        verify(chain, times(30)).doFilter(request, response);
        verify(response, atLeastOnce()).setHeader("X-RateLimit-Limit", "30");
        verify(response, never()).setStatus(429);

        filter.doFilter(request, response, chain);
        verify(chain, times(30)).doFilter(request, response);
        verify(response, times(1)).setStatus(429);
        verify(response, times(1)).setHeader(eq("Retry-After"), anyString());
        // 第 30 次用完配额与第 31 次拒绝都回写 Remaining=0
        verify(response, times(2)).setHeader("X-RateLimit-Remaining", "0");
    }

    @Test
    @DisplayName("管理端登录仍保持 10 次低配额，两条 key 互不占用")
    void adminLoginKeepsLowQuotaAndSeparateBucket() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/admin-login");
        for (int i = 1; i <= 10; i++) {
            filter.doFilter(request, response, chain);
        }
        verify(response, atLeastOnce()).setHeader("X-RateLimit-Limit", "10");

        filter.doFilter(request, response, chain);
        verify(response, times(1)).setStatus(429);

        // 小程序登录不受管理端配额影响
        org.mockito.Mockito.clearInvocations(response, chain);
        when(request.getRequestURI()).thenReturn("/api/v1/auth/wechat-login");
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    @DisplayName("429 带 Retry-After，前端可据此倒计时而不是盲重试")
    void rejectedResponseCarriesRetryAfter() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/admin-login");
        for (int i = 1; i <= 11; i++) {
            filter.doFilter(request, response, chain);
        }
        verify(response).setHeader(eq("Retry-After"), anyString());
    }

    @Test
    @DisplayName("非登录接口不进限流桶，也不回写配额头")
    void otherEndpointsBypassRateLimit() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/orders");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).setHeader(eq("X-RateLimit-Limit"), anyString());
    }

    @Test
    @DisplayName("CORS 预检不消耗配额")
    void optionsPreflightNotCounted() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/wechat-login");
        when(request.getMethod()).thenReturn("OPTIONS");

        for (int i = 0; i < 40; i++) {
            filter.doFilter(request, response, chain);
        }
        verify(chain, times(40)).doFilter(request, response);

        when(request.getMethod()).thenReturn("POST");
        filter.doFilter(request, response, chain);
        verify(chain, times(41)).doFilter(request, response);
        verify(response).setHeader("X-RateLimit-Remaining", "29");
    }
}
