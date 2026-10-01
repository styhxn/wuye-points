package com.wuye.points.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 日志脱敏的回归测试。
 *
 * <p>日志一旦写入文件便难以回收，且日志文件通常不在加密与访问控制的保护范围内，
 * 因此脱敏必须是"默认生效"的。这些断言用于防止后续改动意外把敏感信息打回日志。
 */
class SensitiveMaskerTest {

    @Test
    @DisplayName("手机号脱敏保留前3后4")
    void masksPhone() {
        assertEquals("138****1234", SensitiveMasker.maskPhone("13812341234"));
        assertNull(SensitiveMasker.maskPhone(null));
    }

    @Test
    @DisplayName("银行账号仅保留后 4 位")
    void masksAccountNo() {
        assertEquals("****5678", SensitiveMasker.maskAccountNo("1234567890125678"));
        assertEquals("****", SensitiveMasker.maskAccountNo("12"));
        assertNull(SensitiveMasker.maskAccountNo(null));
    }

    @Test
    @DisplayName("JSON 中的敏感字段整体打码")
    void masksSensitiveJsonKeys() {
        String masked = SensitiveMasker.mask("{\"password\":\"abc123\",\"phone\":\"13812341234\"}");
        assertFalse(masked.contains("abc123"), "密码不应出现在日志中");
        assertFalse(masked.contains("13812341234"), "手机号不应明文出现在日志中");
    }

    @Test
    @DisplayName("自由文本中的手机号与身份证被识别并脱敏")
    void masksPatternsInFreeText() {
        String masked = SensitiveMasker.mask("用户 13812341234 的身份证 11010119900307123X");
        assertFalse(masked.contains("13812341234"));
        assertFalse(masked.contains("19900307"));
    }

    @Test
    @DisplayName("令牌保留前缀便于日志关联，但主体被打码")
    void masksSecret() {
        String secret = "prev_1234567890abcdef";
        String masked = SensitiveMasker.maskSecret(secret);
        assertFalse(masked.contains("1234567890abcdef"));
        assertEquals("****", SensitiveMasker.maskSecret("abc"));
        assertNull(SensitiveMasker.maskSecret(null));
    }

    @Test
    @DisplayName("脱敏工具自身不得抛异常，避免影响主流程")
    void neverThrows() {
        assertEquals("null", SensitiveMasker.mask(null));
        assertEquals("", SensitiveMasker.mask(""));
    }
}
