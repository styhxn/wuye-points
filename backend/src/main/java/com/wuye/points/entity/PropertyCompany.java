package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 物业公司表（含四级分成比例、物业币配置、配送配置、提现配置等）
 */
@Data
@Entity
@Table(name = "property_companies")
@EqualsAndHashCode(callSuper = true)
public class PropertyCompany extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "address", length = 200)
    private String address;

    /** 物业分成比例 */
    @Column(name = "property_share_rate", precision = 5, scale = 4)
    private BigDecimal propertyShareRate;

    /** 统筹分成比例 */
    @Column(name = "coordinator_share_rate", precision = 5, scale = 4)
    private BigDecimal coordinatorShareRate;

    /** 板块负责人比例（从统筹中抽取） */
    @Column(name = "sector_leader_rate", precision = 5, scale = 4)
    private BigDecimal sectorLeaderRate;

    /** 个体负责人比例（从板块中抽取） */
    @Column(name = "individual_leader_rate", precision = 5, scale = 4)
    private BigDecimal individualLeaderRate;

    /** 物业币卡片全局显示 */
    @Column(name = "coin_display_enabled")
    private Boolean coinDisplayEnabled;

    /** 发放模式：auto/manual_approval */
    @Column(name = "coin_issue_mode", length = 20)
    private String coinIssueMode;

    /** 物业币有效期天数 */
    @Column(name = "coin_expiry_days")
    private Integer coinExpiryDays;

    /** 新用户物业币默认冻结 */
    @Column(name = "coin_freeze_default")
    private Boolean coinFreezeDefault;

    /** 配送起步价 */
    @Column(name = "delivery_base_fee", precision = 10, scale = 2)
    private BigDecimal deliveryBaseFee;

    /** 快递员配送费分成比例 */
    @Column(name = "delivery_courier_share_rate", precision = 5, scale = 4)
    private BigDecimal deliveryCourierShareRate;

    /** 快递员每单报酬 */
    @Column(name = "delivery_courier_per_order", precision = 10, scale = 2)
    private BigDecimal deliveryCourierPerOrder;

    /** 提现手续费率 */
    @Column(name = "withdrawal_fee_rate", precision = 5, scale = 4)
    private BigDecimal withdrawalFeeRate;

    /** 积分抵扣物业费比率（100积分=1元则0.01） */
    @Column(name = "point_to_fee_rate", precision = 10, scale = 4)
    private BigDecimal pointToFeeRate;

    /** 2年欠费积分清零开关 */
    @Column(name = "two_year_clear_enabled")
    private Boolean twoYearClearEnabled;

    /** 邻居私聊每日限制 */
    @Column(name = "neighbor_daily_contact_limit")
    private Integer neighborDailyContactLimit;

    /** active/inactive */
    @Column(name = "status", length = 20)
    private String status;

    /** 平台提现抽成比率 */
    @Column(name = "platform_withdrawal_fee_share_rate", precision = 5, scale = 4)
    private BigDecimal platformWithdrawalFeeShareRate;

    /** 区域负责人分成比例 */
    @Column(name = "regional_leader_rate", precision = 5, scale = 4)
    private BigDecimal regionalLeaderRate;

    /** 平台分成比例 */
    @Column(name = "platform_share_rate", precision = 5, scale = 4)
    private BigDecimal platformShareRate;

    /** 物业币商城是否开启 */
    @Column(name = "coin_mall_enabled")
    private Boolean coinMallEnabled;

    /** 物业币商城起购金额 */
    @Column(name = "coin_mall_min_amount", precision = 10, scale = 2)
    private BigDecimal coinMallMinAmount;

    /** 物业币商城最大占比 */
    @Column(name = "coin_mall_max_ratio", precision = 5, scale = 4)
    private BigDecimal coinMallMaxRatio;

    /** 物业币使用条件：none/point_threshold/property_fee_paid/manual_issue */
    @Column(name = "coin_use_condition", length = 30)
    private String coinUseCondition;

    /** 物业币使用条件积分门槛（coin_use_condition=point_threshold 时生效） */
    @Column(name = "coin_point_threshold")
    private Integer coinPointThreshold;

    /** 结算余额 */
    @Column(name = "settlement_balance", precision = 10, scale = 2)
    private BigDecimal settlementBalance;

    /** 公司账户余额 */
    @Column(name = "company_account_balance", precision = 10, scale = 2)
    private BigDecimal companyAccountBalance;

    /** 积分功能开关 */
    @Column(name = "point_enabled")
    private Boolean pointEnabled;

    /** 物业币功能开关 */
    @Column(name = "coin_enabled")
    private Boolean coinEnabled;

    /** 积分归属模式 property(到物业公司账号)/resident(业主自己使用，钱到平台) */
    @Column(name = "points_attribution_mode", length = 20)
    private String pointsAttributionMode;

    /** 奖励金归属模式 property(到物业公司账号)/resident(业主自己使用，需物业同意) */
    @Column(name = "reward_attribution_mode", length = 20)
    private String rewardAttributionMode;

    /** 居民积分分成比例 */
    @Column(name = "resident_point_share_rate", precision = 5, scale = 4)
    private BigDecimal residentPointShareRate;

    /** 商家积分分成比例 */
    @Column(name = "merchant_point_share_rate", precision = 5, scale = 4)
    private BigDecimal merchantPointShareRate;

    /** 物业币分成比例 */
    @Column(name = "coin_point_share_rate", precision = 5, scale = 4)
    private BigDecimal coinPointShareRate;

    /** 共享积分分成比例 */
    @Column(name = "shared_point_share_rate", precision = 5, scale = 4)
    private BigDecimal sharedPointShareRate;

    /** 项目（工程）负责人分成比例 */
    @Column(name = "project_leader_rate", precision = 5, scale = 4)
    private BigDecimal projectLeaderRate;

    /** 平台配送费分成比例 */
    @Column(name = "platform_delivery_share_rate", precision = 5, scale = 4)
    private BigDecimal platformDeliveryShareRate;

    /** 快递员分成比例 */
    @Column(name = "courier_share_rate", precision = 5, scale = 4)
    private BigDecimal courierShareRate;

    /** 自动提现周期天数 */
    @Column(name = "auto_withdrawal_period_days")
    private Integer autoWithdrawalPeriodDays;

    /** 物业公司自动提现开关（per-company） */
    @Column(name = "auto_withdrawal_enabled")
    private Boolean autoWithdrawalEnabled;
}
