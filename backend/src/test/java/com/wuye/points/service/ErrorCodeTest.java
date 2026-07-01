package com.wuye.points.service;

import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ErrorCode 枚举测试")
class ErrorCodeTest {

    @Test
    @DisplayName("所有错误码应有唯一 code")
    void allCodesShouldBeUnique() {
        ErrorCode[] codes = ErrorCode.values();
        long uniqueCount = java.util.Arrays.stream(codes)
                .map(ErrorCode::getCode)
                .distinct()
                .count();
        assertEquals(codes.length, uniqueCount, "错误码 code 必须唯一");
    }

    @Test
    @DisplayName("所有错误码应有非空 message")
    void allMessagesShouldBeNonNull() {
        for (ErrorCode errorCode : ErrorCode.values()) {
            assertNotNull(errorCode.getMessage(), errorCode.name() + " 的 message 不能为 null");
            assertFalse(errorCode.getMessage().isEmpty(), errorCode.name() + " 的 message 不能为空");
        }
    }

    @Test
    @DisplayName("BizException 应正确携带错误码")
    void bizExceptionShouldCarryErrorCode() {
        BizException ex = new BizException(ErrorCode.POINT_INSUFFICIENT);
        assertEquals(40001, ex.getCode());
        assertEquals("积分余额不足", ex.getMessage());
    }

    @Test
    @DisplayName("BizException 自定义消息")
    void bizExceptionWithCustomMessage() {
        BizException ex = new BizException(50001, "自定义余额不足信息");
        assertEquals(50001, ex.getCode());
        assertEquals("自定义余额不足信息", ex.getMessage());
    }

    @Test
    @DisplayName("核心业务错误码范围验证")
    void coreBusinessErrorCodesShouldBeInRange() {
        // 鉴权 20xxx
        assertTrue(ErrorCode.TOKEN_MISSING.getCode() >= 20000 && ErrorCode.TOKEN_MISSING.getCode() < 21000);
        // 业主 30xxx
        assertTrue(ErrorCode.RESIDENT_NOT_FOUND.getCode() >= 30000 && ErrorCode.RESIDENT_NOT_FOUND.getCode() < 31000);
        // 积分 40xxx
        assertTrue(ErrorCode.POINT_INSUFFICIENT.getCode() >= 40000 && ErrorCode.POINT_INSUFFICIENT.getCode() < 41000);
        // 商家 60xxx
        assertTrue(ErrorCode.MERCHANT_NOT_FOUND.getCode() >= 60000 && ErrorCode.MERCHANT_NOT_FOUND.getCode() < 61000);
        // 订单 70xxx
        assertTrue(ErrorCode.ORDER_NOT_FOUND.getCode() >= 70000 && ErrorCode.ORDER_NOT_FOUND.getCode() < 71000);
    }
}
