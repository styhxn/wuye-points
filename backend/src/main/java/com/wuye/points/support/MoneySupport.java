package com.wuye.points.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额与积分的统一计算口径。
 *
 * <p>背景：金额与积分的换算原先散落在 10 余个 Service 中，且规则不一致：
 * <ul>
 *   <li>消费（金额→积分）：{@code OrderService} 用 {@code RoundingMode.UP}、
 *       {@code MerchantPointService} 用 {@code CEILING}，其余多用 UP；</li>
 *   <li>返利（积分→金额 / 返积分）：多用 {@code DOWN}。</li>
 * </ul>
 * 同一用户在不同入口消费相同金额，被扣积分可能相差 1 分；消费向上取整叠加返现向下取整，
 * 长期形成平台侧的隐性截留，对账时无法解释差额。
 *
 * <p>这里把规则显式固化下来：
 * <ul>
 *   <li><b>消费</b>（用户付出）：向上取整 —— 宁可少扣用户积分，避免多扣引发投诉；</li>
 *   <li><b>返利</b>（平台付出）：向下取整 —— 宁可少返，避免超发。</li>
 * </ul>
 * 规则本身不是"更正确"的唯一解，关键在<b>全站口径一致且可追溯</b>。
 */
public final class MoneySupport {

    /** 金额统一保留 2 位小数 */
    public static final int MONEY_SCALE = 2;

    private MoneySupport() {
    }

    /** 金额规范化：2 位小数，四舍五入 */
    public static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /** 安全加法，null 视为 0 */
    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return nz(a).add(nz(b));
    }

    /** 安全减法，null 视为 0 */
    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return nz(a).subtract(nz(b));
    }

    /**
     * 金额 → 积分（消费方向）：向上取整。
     *
     * @param amount    金额（元）
     * @param rate      兑换比例（多少积分抵 1 元，如 100 表示 100 积分 = 1 元）
     */
    public static int toPointsForConsume(BigDecimal amount, BigDecimal rate) {
        if (amount == null || rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return nz(amount).multiply(rate).setScale(0, RoundingMode.UP).intValueExact();
    }

    /**
     * 积分 → 金额（返利方向）：向下取整。
     *
     * @param points 积分
     * @param rate   兑换比例（多少积分抵 1 元）
     */
    public static BigDecimal toAmountForRebate(int points, BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(points)
                .divide(rate, MONEY_SCALE, RoundingMode.DOWN);
    }

    /**
     * 金额 → 积分（返利方向，如消费返积分）：向下取整。
     */
    public static int toPointsForRebate(BigDecimal amount, BigDecimal rate) {
        if (amount == null || rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return nz(amount).multiply(rate).setScale(0, RoundingMode.DOWN).intValueExact();
    }

    /** null → ZERO */
    public static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
