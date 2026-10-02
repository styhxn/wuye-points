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
 * 用户优惠券（券实例）user_coupon
 * <p>含模板快照字段，模板后续变更不影响已发放券。</p>
 */
@Data
@Entity
@Table(name = "user_coupon")
@EqualsAndHashCode(callSuper = true)
public class UserCoupon extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "template_id", length = 32, nullable = false)
    private String templateId;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "merchant_id", length = 32)
    private String merchantId;

    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;

    /** unused / locked / used / expired */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    /** claim 领券中心 / grant 定向发放 */
    @Column(name = "source", length = 20, nullable = false)
    private String source;

    @Column(name = "granted_by", length = 32)
    private String grantedBy;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "type", length = 20)
    private String type;

    @Column(name = "value_amount", precision = 10, scale = 2)
    private BigDecimal valueAmount;

    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate;

    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "min_spend_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal minSpendAmount;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "order_id", length = 32)
    private String orderId;
}