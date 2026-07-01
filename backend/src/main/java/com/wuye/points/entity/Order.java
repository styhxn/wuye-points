package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表 ord_xxxxxxxx
 */
@Data
@Entity
@Table(name = "orders")
@EqualsAndHashCode(callSuper = true)
public class Order extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "order_no", length = 32, unique = true)
    private String orderNo;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "family_id", length = 32)
    private String familyId;

    @Column(name = "order_status", length = 20)
    private String orderStatus;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @Column(name = "point_used")
    private Integer pointUsed;

    @Column(name = "coin_used", precision = 10, scale = 2)
    private BigDecimal coinUsed;

    @Column(name = "cash_amount", precision = 10, scale = 2)
    private BigDecimal cashAmount;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 200)
    private String cancelReason;

    @Column(name = "point_earned")
    private Integer pointEarned;

    @Column(name = "coin_earned", precision = 10, scale = 2)
    private BigDecimal coinEarned;

    @Column(name = "courier_id", length = 32)
    private String courierId;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "remark", length = 500)
    private String remark;

    @Version
    @Column(name = "version")
    private Integer version;
}
