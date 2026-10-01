package com.wuye.points.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 出钱链路 · 订单接口（创建 / 支付试算 / 支付 / 订单列表）的路由与鉴权集成测试。
 *
 * 验证点：
 *  - 无令牌 → 401（AuthInterceptor 生效）
 *  - 错误角色令牌 → 403（RoleGuard 生效）
 *  - 正确角色令牌 → 能抵达业务层（空库下因缺数据返回 4xx，证明路由与鉴权已通过）
 *
 * 订单的金额拆分核心逻辑（payOrder）为 private 方法，纯单测极脆弱，
 * 后续将改用“集成测试 + DB 种子”验证支付/退款金额计算，本类先守住路由与鉴权边界。
 *
 * 全程本地 H2 + 容器内 JwtUtil，不连接任何外部/生产服务。
 */
@DisplayName("出钱链路-订单接口路由与鉴权")
class OrderControllerTest extends ControllerTestBase {

    // ==================== GET /orders/my（resident） ====================
    @Test
    @DisplayName("我的订单: 无令牌 → 401")
    void myOrders_noToken_401() throws Exception {
        mockMvc.perform(get("/orders/my"))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    @DisplayName("我的订单: merchant 令牌 → 403")
    void myOrders_merchant_403() throws Exception {
        mockMvc.perform(get("/orders/my")
                        .header("Authorization", bearer("u_merchant", "merchant", "pc1", null)))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @DisplayName("我的订单: resident 令牌 → 4xx（路由命中业务，空库无业主行）")
    void myOrders_resident_authReached_4xx() throws Exception {
        mockMvc.perform(get("/orders/my")
                        .header("Authorization", bearer("u_resident", "resident", "pc1", "fam1")))
                .andExpect(status().is4xxClientError());
    }

    // ==================== GET /orders（merchant/admin） ====================
    @Test
    @DisplayName("订单列表: 无令牌 → 401")
    void listOrders_noToken_401() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    @DisplayName("订单列表: resident 令牌 → 403")
    void listOrders_resident_403() throws Exception {
        mockMvc.perform(get("/orders")
                        .header("Authorization", bearer("u_resident", "resident", "pc1", "fam1")))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    // ==================== 写操作端点：无令牌一律 401 ====================
    @Test
    @DisplayName("创建订单: 无令牌 → 401")
    void createOrder_noToken_401() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType("application/json").content("{}"))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    @DisplayName("支付试算: 无令牌 → 401")
    void previewOrder_noToken_401() throws Exception {
        mockMvc.perform(post("/orders/preview")
                        .contentType("application/json").content("{}"))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    @DisplayName("支付订单: 无令牌 → 401")
    void payOrder_noToken_401() throws Exception {
        mockMvc.perform(post("/orders/1/pay")
                        .contentType("application/json").content("{}"))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }
}
