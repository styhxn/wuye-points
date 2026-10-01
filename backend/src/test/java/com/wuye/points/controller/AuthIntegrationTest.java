package com.wuye.points.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 鉴权集成测试：验证应用自身的 AuthInterceptor + RoleGuard 对令牌的处理。
 *
 * 注意：测试通过 @AutoConfigureMockMvc(addFilters = false) 关闭了 Spring Security 过滤器链，
 * 以便隔离验证“应用自身”的鉴权逻辑（这才是我们要测的），而非第二层安全框架的统一拦截。
 *
 * 全程本地 H2 + 容器内 JwtUtil，不连接任何外部/生产服务。
 */
@DisplayName("鉴权集成测试")
class AuthIntegrationTest extends ControllerTestBase {

    @Test
    @DisplayName("商家专属端点缺少令牌 → 401 TOKEN_MISSING")
    void merchantEndpoint_noToken_returns401() throws Exception {
        mockMvc.perform(get("/merchant/withdrawals"))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    @DisplayName("商家专属端点使用 resident 令牌 → 403 PERMISSION_DENIED")
    void merchantEndpoint_residentToken_returns403() throws Exception {
        mockMvc.perform(get("/merchant/withdrawals")
                        .header("Authorization", bearer("u_resident", "resident", "pc1", "fam1")))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @DisplayName("非 Bearer 格式的令牌 → 401")
    void merchantEndpoint_malformedToken_returns401() throws Exception {
        mockMvc.perform(get("/merchant/withdrawals")
                        .header("Authorization", "Basic abcdef"))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }
}
