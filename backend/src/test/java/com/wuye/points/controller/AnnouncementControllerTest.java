package com.wuye.points.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 公告接口集成测试：验证“令牌 → AuthInterceptor → UserContext → RoleGuard → Service → 200”完整链路。
 *
 * 说明：公告列表接口在 Service 内部要求登录，因此使用 platform_admin 令牌走通
 * （platform_admin 分支不依赖居民行，空库下直接返回空分页 200），无需任何种子数据。
 *
 * 全程本地 H2 + 容器内 JwtUtil，不连接任何外部/生产服务。
 */
@DisplayName("公告接口集成测试")
class AnnouncementControllerTest extends ControllerTestBase {

    @Test
    @DisplayName("GET /announcements 携带 platform_admin 令牌返回 200（空库空分页）")
    void listAnnouncements_authenticated_ok() throws Exception {
        mockMvc.perform(get("/announcements")
                        .header("Authorization", bearer("u_admin", "platform_admin", "pc1", null)))
                .andExpect(status().is(HttpStatus.OK.value()));
    }
}
