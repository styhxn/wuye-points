package com.wuye.points.support;

import java.math.BigDecimal;

/**
 * 分账/费率相关默认值的唯一来源。
 *
 * <p>背景：这些默认值原先散落在 10 余个 Service 中各自声明，其中存在<b>同名不同义</b>：
 * {@code DEFAULT_SECTOR_LEADER_RATE} 在 DistributionService（分账比例）中为 0.6000，
 * 在 CoordinatorWorkbenchService（负责人佣金率）中却是 0.1000 ——
 * 两者是不同业务概念，却共用同一个名字，极易在后续维护中被误当成"同一个值"而统一，
 * 从而造成分账金额错配。
 *
 * <p>本类按语义把两者拆开命名：
 * <ul>
 *   <li>{@code *_RATE}：分账比例，即各方从订单金额中分得的比例；</li>
 *   <li>{@code *_COMMISSION_RATE}：负责人佣金率，即负责人从其管理盘收入中抽取的佣金。</li>
 * </ul>
 * 数值与重构前<b>完全一致</b>，本次不改行为，只消除命名歧义与多处定义。
 *
 * <p>所有 Service 必须从这里取值，禁止再自行声明同名常量。
 * 这些值仅在数据库未配置时作为兜底。
 */
public final class RateDefaults {

    // ---------------- 分账比例（各方从订单金额中分得的比例） ----------------

    /** 板块负责人分成比例默认值 */
    public static final BigDecimal SECTOR_LEADER_RATE = new BigDecimal("0.6000");

    /** 个体负责人分成比例默认值 */
    public static final BigDecimal INDIVIDUAL_LEADER_RATE = new BigDecimal("0.5000");

    /** 统筹（管理盘）分成比例默认值 */
    public static final BigDecimal COORDINATOR_RATE = new BigDecimal("0.3000");

    /** 物业分成比例默认值 */
    public static final BigDecimal PROPERTY_RATE = new BigDecimal("0.1000");

    /** 平台分成比例默认值 */
    public static final BigDecimal PLATFORM_RATE = new BigDecimal("0.1500");

    /** 配送员分成比例默认值（A 方案四档时使用） */
    public static final BigDecimal COURIER_RATE = new BigDecimal("0.6000");

    /** 提现手续费率默认值 */
    public static final BigDecimal WITHDRAWAL_FEE_RATE = new BigDecimal("0.0060");

    /** 积分抵物业费的比例默认值：1 积分 = 0.01 元 */
    public static final BigDecimal POINT_TO_FEE_RATE = new BigDecimal("0.0100");

    /** 积分兑换比例默认值：100 积分 = 1 元 */
    public static final BigDecimal POINT_EXCHANGE_RATE = new BigDecimal("100.0000");

    /** 商家挂接抽佣比例默认值（merchant_property_configs.commission_rate） */
    public static final BigDecimal MERCHANT_COMMISSION_RATE = new BigDecimal("0.1000");

    /** 商家挂接积分兑换比默认值（merchant_property_configs.point_exchange_rate） */
    public static final BigDecimal MERCHANT_POINT_EXCHANGE_RATE = POINT_EXCHANGE_RATE;

    // ---------------- 负责人佣金率（从管理盘收入中抽取的佣金） ----------------

    /** 板块负责人佣金率默认值 */
    public static final BigDecimal SECTOR_LEADER_COMMISSION_RATE = new BigDecimal("0.1000");

    /** 个体负责人佣金率默认值 */
    public static final BigDecimal INDIVIDUAL_LEADER_COMMISSION_RATE = BigDecimal.ZERO;

    /** 比例求和的容差：用于校验"分成比例合计为 100%"时的浮点误差 */
    public static final BigDecimal RATE_TOLERANCE = new BigDecimal("0.0001");

    private RateDefaults() {
    }

    /**
     * 取配置值，未配置（为 null 或非正）时返回默认值。
     *
     * @param configured 数据库中配置的比例
     * @param fallback   兜底默认值
     */
    public static BigDecimal orDefault(BigDecimal configured, BigDecimal fallback) {
        return configured != null && configured.compareTo(BigDecimal.ZERO) > 0
                ? configured : fallback;
    }
}
