package com.wuye.points.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("交易明细标签映射")
class TransactionLabelsTest {

    @Test
    @DisplayName("分类/单位/方向有中文标签，未知值原样返回")
    void basicLabels() {
        assertEquals("支付", TransactionLabels.categoryLabel("payment"));
        assertEquals("物业币", TransactionLabels.unitLabel("COIN"));
        assertEquals("支出", TransactionLabels.directionLabel("expense"));
        assertEquals("unknown", TransactionLabels.categoryLabel("unknown"));
    }

    @Test
    @DisplayName("不同账本的同名状态按分类翻译")
    void statusLabels() {
        assertEquals("待支付", TransactionLabels.statusLabel("payment", "pending"));
        assertEquals("待审核", TransactionLabels.statusLabel("withdrawal", "pending_audit"));
        assertEquals("已冲正", TransactionLabels.statusLabel("wallet", "reversed"));
        assertEquals("消费扣减", TransactionLabels.statusLabel("canteen", "consume_deduct"));
        assertEquals("raw_status", TransactionLabels.statusLabel("payment", "raw_status"));
    }

    @Test
    @DisplayName("业务类型与主体类型有中文标签")
    void typeLabels() {
        assertEquals("分账冲正", TransactionLabels.typeLabel("wallet", "distribution_reversal"));
        assertEquals("商家提现", TransactionLabels.typeLabel("withdrawal", "merchant_withdrawal"));
        assertEquals("订单分账", TransactionLabels.typeLabel("distribution", "order_distribution"));
        assertEquals("平台", TransactionLabels.ownerTypeLabel("PLATFORM"));
    }
}
