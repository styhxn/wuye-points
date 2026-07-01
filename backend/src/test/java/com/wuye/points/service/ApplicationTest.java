package com.wuye.points.service;

import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.util.RoleGuard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("核心类编译与基本功能测试")
class ApplicationTest {

    @Test
    @DisplayName("ErrorCode 枚举可正常访问")
    void errorCodeEnumShouldWork() {
        assertEquals(99001, ErrorCode.SYSTEM_ERROR.getCode());
        assertNotNull(ErrorCode.values());
        assertTrue(ErrorCode.values().length > 30);
    }

    @Test
    @DisplayName("BizException 可正常创建")
    void bizExceptionShouldWork() {
        BizException ex = new BizException(ErrorCode.ORDER_NOT_FOUND);
        assertEquals(70001, ex.getCode());
        assertEquals("订单不存在", ex.getMessage());
    }

    @Test
    @DisplayName("RoleGuard 枚举常量完整性")
    void roleGuardConstantsShouldBeComplete() {
        assertEquals(15, RoleGuard.FAMILY_MEMBER_MAX);
        assertTrue(RoleGuard.ORDER_STATUS.contains("pending"));
        assertTrue(RoleGuard.ORDER_STATUS.contains("completed"));
        assertTrue(RoleGuard.MERCHANT_LEVELS.contains("official_certified"));
        assertTrue(RoleGuard.ANNOUNCEMENT_TYPES.contains("property"));
        assertTrue(RoleGuard.VOTE_OPTIONS.contains("support"));
        assertTrue(RoleGuard.MESSAGE_TYPES.contains("resident_message"));
        assertTrue(RoleGuard.SERVICE_CATEGORIES.contains("cleaning_repair"));
    }
}
