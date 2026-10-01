package com.wuye.points.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Controller 集成测试基类。
 *
 * 使用真实 Spring 上下文 + MockMvc（不启动真实 Servlet 容器），数据走测试库 H2（MySQL 兼容模式），
 * 全程在本地进行，不连接任何外部/生产服务。
 *
 * 鉴权令牌由容器内的 JwtUtil 真实生成，并经 AuthInterceptor → UserContext 注入，
 * 从而端到端验证“路由 + 拦截器 + 角色守卫 + Service”的真实协作。
 *
 * 类名不带 Test 后缀，避免被 Surefire 误识别为可执行的测试类。
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class ControllerTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtUtil jwtUtil;

    /** 生成带 Bearer 前缀的访问令牌 */
    protected String bearer(String userId, String role, String propertyCompanyId, String familyId) {
        return "Bearer " + jwtUtil.generateAccessToken(userId, role, propertyCompanyId, familyId);
    }

    @BeforeEach
    void baseSetUp() {
        // 每个用例前清空线程上下文，避免串味（拦截器 afterCompletion 也会清，这里双保险）
        com.wuye.points.context.UserContext.clear();
    }
}
