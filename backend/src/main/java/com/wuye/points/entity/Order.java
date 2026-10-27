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

    /** 取消来源：user 用户取消 / system 系统超时 / merchant 商家拒单 */
    @Column(name = "cancelled_by", length = 20)
    private String cancelledBy;

    /** 退款状态：null 无退款 / refunding 退款中 / refunded 已退款 / failed 退款失败 */
    @Column(name = "refund_status", length = 20)
    private String refundStatus;

    @Column(name = "refund_fail_reason", length = 255)
    private String refundFailReason;

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

    /** 订单来源：mall=商城/店铺直购，distributor=业主小店代销一级代理货源 */
    @Column(name = "source_type", length = 20)
    private String sourceType;

    /** 一级代理商商家ID（source_type=distributor 时必填，订单/结算面向一级代理商） */
    @Column(name = "distributor_id", length = 32)
    private String distributorId;

    /** 业主小店店铺主体ID（代销场景仅作展示与佣金归属，不作为结算主体） */
    @Column(name = "resident_merchant_id", length = 32)
    private String residentMerchantId;

    /** 结算归属方：merchant/distributor */
    @Column(name = "settlement_owner", length = 20)
    private String settlementOwner;

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

    /** 使用的用户优惠券ID（user_coupon.id） */
    @Column(name = "coupon_id", length = 32)
    private String couponId;

    /** 优惠券抵扣金额（由商家承担；佣金基数按 totalAmount - couponAmount 计算） */
    @Column(name = "coupon_amount", precision = 10, scale = 2)
    private BigDecimal couponAmount;

    /** 下单时绑定的单元楼长ID（快照，订单产生即固定，后续变更楼长不影响本单） */
    @Column(name = "building_leader_id", length = 32)
    private String buildingLeaderId;

    /** 下单时楼长分成比例快照（占物业公司分成的比例） */
    @Column(name = "building_leader_rate", precision = 5, scale = 4)
    private BigDecimal buildingLeaderRate;

    /** 下单时绑定的物业经理ID快照（pm_ 前缀，来自 property_managers） */
    @Column(name = "manager_id", length = 32)
    private String managerId;

    /** 下单时物业经理抽成比例快照（占楼长分成的比例） */
    @Column(name = "manager_rate", precision = 5, scale = 4)
    private BigDecimal managerRate;

    /** 满额减免前基础配送费快照（规范§7.6：结算不得读取最新配置重算） */
    @Column(name = "original_delivery_fee", precision = 10, scale = 2)
    private BigDecimal originalDeliveryFee;

    /** 配送费减免承担方快照：none/merchant/property/platform（规范§7.6，默认 merchant） */
    @Column(name = "delivery_subsidy_sponsor", length = 20)
    private String deliverySubsidySponsor;

    /** 配送补贴金额快照（= 满额减免额，注入配送结算链路） */
    @Column(name = "delivery_subsidy_amount", precision = 10, scale = 2)
    private BigDecimal deliverySubsidyAmount;
}
