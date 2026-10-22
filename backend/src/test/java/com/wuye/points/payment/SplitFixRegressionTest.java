package com.wuye.points.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 分账修复回归测试（P0-0 / P0-1 / P0-3 / P2-9 相关纯函数）。
 *
 * <p>覆盖：电商回退单号确定性（幂等重试）、元→分换算、接收方构建。
 * 有状态编排（WechatSplitService / SplitReconcileScheduler）依赖仓储与微信 HTTP，
 * 属集成测试范围，未纳入本单元测试。</p>
 */
class SplitFixRegressionTest {

    // ---- P0-3：电商回退 out_return_no 确定性生成 ----

    @Test
    @DisplayName("returnNoOf：同一 splitNo 永远生成同一回退单号（重试幂等）")
    void returnNoOfIsDeterministic() {
        assertEquals("ret_sp_abc123", WechatSplitService.returnNoOf("sp_abc123"));
        assertEquals(WechatSplitService.returnNoOf("sp_abc123"),
                WechatSplitService.returnNoOf("sp_abc123"));
    }

    @Test
    @DisplayName("returnNoOf：不同 splitNo 生成不同回退单号（微信侧唯一性）")
    void returnNoOfIsUniquePerSplit() {
        assertNotEquals(WechatSplitService.returnNoOf("sp_1"), WechatSplitService.returnNoOf("sp_2"));
    }

    // ---- 元 → 分换算（冻结 / 回退 / 对账共用） ----

    @Test
    @DisplayName("fen：元转分 HALF_UP，null 安全")
    void fenConversion() {
        assertEquals("0", WechatSplitApiService.fen(null));
        assertEquals("0", WechatSplitApiService.fen(BigDecimal.ZERO));
        assertEquals("1", WechatSplitApiService.fen(new BigDecimal("0.01")));
        assertEquals("100", WechatSplitApiService.fen(new BigDecimal("1")));
        assertEquals("101", WechatSplitApiService.fen(new BigDecimal("1.005")));
        assertEquals("5", WechatSplitApiService.fen(new BigDecimal("0.05")));
    }

    // ---- 电商分账接收方构建 ----

    @Test
    @DisplayName("receiver：type/account/amount(分)/description 完整构建")
    void receiverBuild() {
        Map<String, Object> r = EcommerceSplitApiService.receiver(
                "PERSONAL_OPENID", "oOpenid123", new BigDecimal("2.50"), "订单配送费");
        assertEquals("PERSONAL_OPENID", r.get("type"));
        assertEquals("oOpenid123", r.get("account"));
        assertEquals(250, r.get("amount"));
        assertEquals("订单配送费", r.get("description"));
    }

    @Test
    @DisplayName("receiver：空白描述省略字段；receiversOf 过滤 null")
    void receiverEdgeCases() {
        Map<String, Object> r = EcommerceSplitApiService.receiver(
                "MERCHANT_ID", "1234567890", new BigDecimal("0.01"), "   ");
        assertFalse(r.containsKey("description"));

        List<Map<String, Object>> list = EcommerceSplitApiService.receiversOf(r, null);
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("receiver：金额为整数分（HALF_UP，无 BigDecimal 残留）")
    void receiverAmountIsIntegralFen() {
        Map<String, Object> r = EcommerceSplitApiService.receiver(
                "MERCHANT_ID", "1234567890", new BigDecimal("1.005"), "佣金");
        assertEquals(101, r.get("amount"));
        assertTrue(r.get("amount") instanceof Integer);
    }
}
