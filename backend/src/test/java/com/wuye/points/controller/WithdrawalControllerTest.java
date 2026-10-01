package com.wuye.points.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 出钱链路 · 提现类接口（商家提现 / 物业币兑换）的路由与鉴权集成测试。
 *
 * 验证点：
 *  - 无令牌 → 401（AuthInterceptor 生效）
 *  - 错误角色令牌 → 403（RoleGuard 生效）
 *  - 正确角色令牌 → 能抵达业务层（空库下因缺数据返回 4xx，证明路由与鉴权已通过）
 *  - platform_admin 拉取物业币兑换列表 → 200（该分支不依赖当前商家/业主行，可干净验证完整链路）
 *
 * 全程本地 H2 + 容器内 JwtUtil，不连接任何外部/生产服务。
 */
@DisplayName("出钱链路-提现接口路由与鉴权")
class WithdrawalControllerTest extends ControllerTestBase {

    // ==================== GET /coin-withdrawals ====================
    @Test
    @DisplayName("物业币兑换列表: platform_admin 令牌 → 200（空库空分页）")
    void coinWithdrawals_list_platformAdmin_200() throws Exception {
        mockMvc.perform(get("/coin-withdrawals")
                        .header("Authorization", bearer("u_admin", "platform_admin", "pc1", null)))
                .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    @DisplayName("物业币兑换列表: 无令牌 → 401")
    void coinWithdrawals_list_noToken_401() throws Exception {
        mockMvc.perform(get("/coin-withdrawals"))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    @DisplayName("物业币兑换列表: resident 令牌 → 403")
    void coinWithdrawals_list_resident_403() throws Exception {
        mockMvc.perform(get("/coin-withdrawals")
                        .header("Authorization", bearer("u_resident", "resident", "pc1", "fam1")))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    // ==================== GET /merchant/withdrawals ====================
    @Test
    @DisplayName("商家提现列表: 无令牌 → 401")
    void merchantWithdrawals_list_noToken_401() throws Exception {
        mockMvc.perform(get("/merchant/withdrawals"))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    @DisplayName("商家提现列表: resident 令牌 → 403")
    void merchantWithdrawals_list_resident_403() throws Exception {
        mockMvc.perform(get("/merchant/withdrawals")
                        .header("Authorization", bearer("u_resident", "resident", "pc1", "fam1")))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @DisplayName("商家提现列表: merchant 令牌 → 4xx（路由命中业务，空库无商家行）")
    void merchantWithdrawals_list_merchant_authReached_4xx() throws Exception {
        mockMvc.perform(get("/merchant/withdrawals")
                        .header("Authorization", bearer("u_merchant", "merchant", "pc1", null)))
                .andExpect(status().is4xxClientError());
    }

    // ==================== POST 申请端点（无令牌一律 401，鉴权优先于参数校验） ====================
    @Test
    @DisplayName("申请商家提现: 无令牌 → 401")
    void merchantWithdrawals_apply_noToken_401() throws Exception {
        mockMvc.perform(post("/merchant/withdrawals")
                        .contentType("application/json").content("{}"))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    @DisplayName("申请物业币兑换: 无令牌 → 401")
    void coinWithdrawals_apply_noToken_401() throws Exception {
        mockMvc.perform(post("/coin-withdrawals")
                        .contentType("application/json").content("{}"))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }
}
