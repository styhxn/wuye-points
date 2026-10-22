package com.wuye.points.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 上线前加固逻辑回归测试（2026-10-18）：
 * <ul>
 *   <li>分账单号确定性生成（微信幂等键，防重跑/补分/崩溃恢复重复出钱）；</li>
 *   <li>元分换算边界（既有能力，随加固一并守护）。</li>
 * </ul>
 */
class SplitHardeningTest {

    @Test
    @DisplayName("splitNoOf：同一订单同一主体永远生成同一分账单号（微信侧幂等）")
    void splitNoOfIsDeterministic() {
        assertEquals(WechatSplitService.splitNoOf("ord_1", "COURIER"),
                WechatSplitService.splitNoOf("ord_1", "COURIER"));
    }

    @Test
    @DisplayName("splitNoOf：不同订单/不同主体生成不同单号（微信侧唯一性）")
    void splitNoOfIsUnique() {
        assertNotEquals(WechatSplitService.splitNoOf("ord_1", "COURIER"),
                WechatSplitService.splitNoOf("ord_2", "COURIER"));
        assertNotEquals(WechatSplitService.splitNoOf("ord_1", "COURIER"),
                WechatSplitService.splitNoOf("ord_1", "PLATFORM"));
    }

    @Test
    @DisplayName("splitNoOf：最长主体名（INDIVIDUAL_LEADER）+32位订单号仍在微信 64 字符上限内，且字符集合法")
    void splitNoOfRespectsWechatLimits() {
        String orderId = "ord_" + "9".repeat(28); // 32 字符（订单号列宽）
        String splitNo = WechatSplitService.splitNoOf(orderId, "INDIVIDUAL_LEADER");
        assertTrue(splitNo.length() <= 64,
                () -> "splitNo 长度 " + splitNo.length() + " 超过微信 64 上限: " + splitNo);
        // 微信 out_order_no 字符集：[0-9a-zA-Z_*@-]
        assertTrue(splitNo.matches("[0-9a-zA-Z_*@-]+"),
                () -> "splitNo 含非法字符: " + splitNo);
        // 回退单号 = splitNo + "_RET"，同样受 64 上限约束
        assertTrue((splitNo + "_RET").length() <= 64);
    }

    @Test
    @DisplayName("fen：元分换算 HALF_UP 边界（既有能力守护）")
    void fenBoundaries() {
        assertEquals("0", WechatSplitApiService.fen(null));
        assertEquals("0", WechatSplitApiService.fen(java.math.BigDecimal.ZERO));
        assertEquals("1", WechatSplitApiService.fen(new java.math.BigDecimal("0.01")));
        assertEquals("101", WechatSplitApiService.fen(new java.math.BigDecimal("1.005")));
        assertEquals("5", WechatSplitApiService.fen(new java.math.BigDecimal("0.05")));
    }
}
