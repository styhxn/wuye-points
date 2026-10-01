package com.wuye.points.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 错误码表的完整性校验。
 *
 * <p>历史上 97011 曾被"催缴落库失败"与"配送已结算"两个语义完全不同的错误码共用，
 * 导致前端无法区分、HTTP 状态码映射错误。枚举初始化时已会检测重复，
 * 这里再从测试角度锁死，并校验每个错误码都显式声明了 HTTP 状态码。
 */
class ErrorCodeIntegrityTest {

    @Test
    @DisplayName("错误码全局唯一")
    void codesAreUnique() {
        Set<Integer> seen = new HashSet<>();
        for (ErrorCode code : ErrorCode.values()) {
            assertTrue(seen.add(code.getCode()),
                    "错误码重复: " + code.getCode() + " (" + code.name() + ")");
        }
    }

    @Test
    @DisplayName("每个错误码都必须声明 HTTP 状态码，避免静默降级为 500")
    void everyCodeHasHttpStatus() {
        for (ErrorCode code : ErrorCode.values()) {
            assertNotNull(code.getHttpStatus(), code.name() + " 缺少 HttpStatus");
        }
    }

    @Test
    @DisplayName("错误码与提示文案均不得为空")
    void codeAndMessageArePresent() {
        for (ErrorCode code : ErrorCode.values()) {
            assertTrue(code.getCode() > 0, code.name() + " 错误码非法");
            assertFalse(code.getMessage() == null || code.getMessage().isBlank(),
                    code.name() + " 缺少提示文案");
        }
    }

    @Test
    @DisplayName("按数字码可反查枚举，未登记的码回退 500")
    void lookupByCode() {
        assertTrue(ErrorCode.findByCode(ErrorCode.PARAM_INVALID.getCode()).isPresent());
        assertFalse(ErrorCode.findByCode(-1).isPresent());
    }
}
