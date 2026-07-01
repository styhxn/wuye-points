package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.util.RoleGuard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RoleGuard 角色校验测试")
class RoleGuardTest {

    private final RoleGuard roleGuard = new RoleGuard();

    @AfterEach
    void cleanup() {
        UserContext.clear();
    }

    private void setUserContext(String role, String propertyCompanyId) {
        UserContext.set(UserContext.builder()
                .userId("res_test001")
                .role(role)
                .propertyCompanyId(propertyCompanyId)
                .familyId("fam_test001")
                .build());
    }

    @Test
    @DisplayName("requireAnyRole - 匹配角色通过")
    void requireAnyRole_shouldPass_whenRoleMatches() {
        setUserContext("property_admin", "pc_001");
        assertDoesNotThrow(() -> roleGuard.requireAnyRole("property_admin", "platform_admin"));
    }

    @Test
    @DisplayName("requireAnyRole - 不匹配角色抛异常")
    void requireAnyRole_shouldThrow_whenRoleNotMatch() {
        setUserContext("resident", "pc_001");
        BizException ex = assertThrows(BizException.class,
                () -> roleGuard.requireAnyRole("property_admin", "platform_admin"));
        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("isPlatformAdmin 正确识别")
    void isPlatformAdmin_shouldReturnTrue() {
        setUserContext("platform_admin", "pc_001");
        assertTrue(roleGuard.isPlatformAdmin());
    }

    @Test
    @DisplayName("isPropertyAdmin 正确识别")
    void isPropertyAdmin_shouldReturnTrue() {
        setUserContext("property_admin", "pc_001");
        assertTrue(roleGuard.isPropertyAdmin());
    }

    @Test
    @DisplayName("resolveListPropertyCompanyId - platform_admin 可跨物业")
    void resolveListPropertyCompanyId_platformAdmin_shouldReturnRequested() {
        setUserContext("platform_admin", "pc_001");
        String result = roleGuard.resolveListPropertyCompanyId("pc_002");
        assertEquals("pc_002", result);
    }

    @Test
    @DisplayName("resolveListPropertyCompanyId - property_admin 限定本物业")
    void resolveListPropertyCompanyId_propertyAdmin_shouldReturnOwn() {
        setUserContext("property_admin", "pc_001");
        String result = roleGuard.resolveListPropertyCompanyId("pc_002");
        assertEquals("pc_001", result);
    }

    @Test
    @DisplayName("assertSamePropertyOrPlatform - 同物业通过")
    void assertSamePropertyOrPlatform_sameProperty_shouldPass() {
        setUserContext("property_admin", "pc_001");
        assertDoesNotThrow(() -> roleGuard.assertSamePropertyOrPlatform("pc_001"));
    }

    @Test
    @DisplayName("assertSamePropertyOrPlatform - 不同物业抛异常")
    void assertSamePropertyOrPlatform_differentProperty_shouldThrow() {
        setUserContext("property_admin", "pc_001");
        assertThrows(BizException.class, () -> roleGuard.assertSamePropertyOrPlatform("pc_002"));
    }

    @Test
    @DisplayName("assertSamePropertyOrPlatform - platform_admin 跨物业通过")
    void assertSamePropertyOrPlatform_platformAdmin_shouldPass() {
        setUserContext("platform_admin", "pc_001");
        assertDoesNotThrow(() -> roleGuard.assertSamePropertyOrPlatform("pc_002"));
    }

    @Test
    @DisplayName("assertSelfOrAdmin - 本人通过")
    void assertSelfOrAdmin_self_shouldPass() {
        setUserContext("resident", "pc_001");
        assertDoesNotThrow(() -> roleGuard.assertSelfOrAdmin("res_test001"));
    }

    @Test
    @DisplayName("assertSelfOrAdmin - 管理员通过")
    void assertSelfOrAdmin_admin_shouldPass() {
        setUserContext("property_admin", "pc_001");
        assertDoesNotThrow(() -> roleGuard.assertSelfOrAdmin("res_other"));
    }

    @Test
    @DisplayName("assertSelfOrAdmin - 非本人非管理员抛异常")
    void assertSelfOrAdmin_nonSelfNonAdmin_shouldThrow() {
        setUserContext("resident", "pc_001");
        assertThrows(BizException.class, () -> roleGuard.assertSelfOrAdmin("res_other"));
    }

    @Test
    @DisplayName("枚举值常量正确性")
    void enumConstants_shouldBeCorrect() {
        assertTrue(RoleGuard.ORDER_STATUS.contains("pending"));
        assertTrue(RoleGuard.ORDER_STATUS.contains("completed"));
        assertTrue(RoleGuard.MERCHANT_LEVELS.contains("official_certified"));
        assertTrue(RoleGuard.ANNOUNCEMENT_TYPES.contains("property"));
        assertEquals(15, RoleGuard.FAMILY_MEMBER_MAX);
    }
}
