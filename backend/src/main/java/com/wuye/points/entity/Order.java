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

    /** 通过业主商户上架商品（resident_merchant_listings）成交时的上架ID */
    @Column(name = "listing_id", length = 32)
    private String listingId;

    /** 定向商城主商家ID */
    @Column(name = "directed_main_merchant_id", length = 32)
    private String directedMainMerchantId;

    /** 验证截止时间 */
    @Column(name = "verification_deadline")
    private LocalDateTime verificationDeadline;

    /** 已验证时间 */
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    /** 验证人 */
    @Column(name = "verified_by", length = 32)
    private String verifiedBy;

    /** 冻结结束时间 */
    @Column(name = "freeze_end_date")
    private LocalDateTime freezeEndDate;

    /** 验证备注 */
    @Column(name = "verification_remark", length = 500)
    private String verificationRemark;

    /** 是否需要配送（团购单/社区食堂单为 false） */
    @Column(name = "requires_delivery")
    private Boolean requiresDelivery;

    /** 履约方式：pending_choice/merchant_self/courier_hall/none（见 FulfillmentConstants） */
    @Column(name = "fulfillment_mode", length = 20)
    private String fulfillmentMode;

    /** 履约方式选择截止时间（超时默认改派平台配送） */
    @Column(name = "fulfillment_deadline")
    private LocalDateTime fulfillmentDeadline;

    /** 履约方式选定时间 */
    @Column(name = "fulfillment_chosen_at")
    private LocalDateTime fulfillmentChosenAt;
}
