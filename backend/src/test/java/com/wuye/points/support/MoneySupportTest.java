package com.wuye.points.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 金额与积分换算口径的回归测试。
 *
 * <p>重构前各 Service 的取整方向不一致（消费侧 UP / CEILING 混用，返利侧 DOWN），
 * 同一用户在不同入口消费相同金额可能被扣不同积分。这里把规则固化下来。
 */
class MoneySupportTest {

    private static final BigDecimal RATE = new BigDecimal("100");

    @Test
    @DisplayName("消费方向：金额转积分向上取整（宁可少扣用户积分）")
    void consumeRoundsUp() {
        // 0.101 元 * 100 = 10.1 分 → 向上取 11
        assertEquals(11, MoneySupport.toPointsForConsume(new BigDecimal("0.101"), RATE));
        // 整除时不额外进位
        assertEquals(10, MoneySupport.toPointsForConsume(new BigDecimal("0.10"), RATE));
    }

    @Test
    @DisplayName("返利方向：金额转积分向下取整（宁可少返，避免超发）")
    void rebateRoundsDown() {
        assertEquals(10, MoneySupport.toPointsForRebate(new BigDecimal("0.109"), RATE));
    }

    @Test
    @DisplayName("积分转金额向下取整")
    void pointsToAmountRoundsDown() {
        assertEquals(new BigDecimal("0.33"), MoneySupport.toAmountForRebate(33, RATE));
    }

    @Test
    @DisplayName("非法比例返回 0 而不是抛异常")
    void invalidRateReturnsZero() {
        assertEquals(0, MoneySupport.toPointsForConsume(new BigDecimal("1"), BigDecimal.ZERO));
        assertEquals(BigDecimal.ZERO, MoneySupport.toAmountForRebate(10, null));
    }

    @Test
    @DisplayName("金额统一 2 位小数四舍五入，null 视为 0")
    void moneyNormalization() {
        assertEquals(0, new BigDecimal("1.24").compareTo(MoneySupport.money(new BigDecimal("1.235"))));
        assertEquals(BigDecimal.ZERO, MoneySupport.money(null));
        // 注意：BigDecimal 的 scale 会随运算变化（1.5+1.5=3.0），比较金额一律用 compareTo 而非 equals
        assertEquals(0, new BigDecimal("3").compareTo(
                MoneySupport.add(new BigDecimal("1.5"), new BigDecimal("1.5"))));
    }
}
