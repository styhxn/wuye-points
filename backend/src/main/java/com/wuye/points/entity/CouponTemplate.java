package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板 coupon_template
 * <p>券类型 type：full_reduction 满减 / discount 折扣 / instant_reduction 立减</p>
 * <p>适用范围 scope：platform 平台券（全平台通用） / merchant 商家券（限指定商家）</p>
 */
@Data
@Entity
@Table(name = "coupon_template")
@EqualsAndHashCode(callSuper = true)
public class CouponTemplate extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    /** full_reduction / discount / instant_reduction */
    @Column(name = "type", length = 20, nullable = false)
    private String type;

    /** platform / merchant */
    @Column(name = "scope", length = 20, nullable = false)
    private String scope;

    @Column(name = "merchant_id", length = 32)
    private String merchantId;

    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;

    /** 满减/立减面额 */
    @Column(name = "value_amount", precision = 10, scale = 2)
    private BigDecimal valueAmount;

    /** 折扣率：8.50 表示 8.5 折 */
    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate;

    /** 折扣券封顶金额 */
    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "min_spend_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal minSpendAmount;

    /** fixed 固定起止 / relative 领取后N天 */
    @Column(name = "valid_type", length = 20, nullable = false)
    private String validType;

    @Column(name = "valid_start_at")
    private LocalDateTime validStartAt;

    @Column(name = "valid_end_at")
    private LocalDateTime validEndAt;

    @Column(name = "valid_days")
    private Integer validDays;

    /** 发放总量，NULL=不限量 */
    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "claimed_quantity", nullable = false)
    private Integer claimedQuantity;

    @Column(name = "per_user_limit", nullable = false)
    private Integer perUserLimit;

    /** active / inactive */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "created_by", length = 32)
    private String createdBy;
}