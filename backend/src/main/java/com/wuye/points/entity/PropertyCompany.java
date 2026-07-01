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
}
