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
 * 配送表
 * 对应 schema.sql 中 deliveries 表（id 前缀 dlv_）
 */
@Data
@Entity
@Table(name = "deliveries")
@EqualsAndHashCode(callSuper = true)
public class Delivery extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "order_id", length = 32, nullable = false)
    private String orderId;

    @Column(name = "courier_id", length = 32)
    private String courierId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "fee", precision = 10, scale = 2, nullable = false)
    private BigDecimal fee;

    @Column(name = "courier_earning", precision = 10, scale = 2)
    private BigDecimal courierEarning;

    @Column(name = "pickup_address", length = 200)
    private String pickupAddress;

    @Column(name = "delivery_address", length = 200)
    private String deliveryAddress;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "picked_at")
    private LocalDateTime pickedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    /** 商家配送费分成 */
    @Column(name = "merchant_delivery_fee_share", precision = 10, scale = 2)
    private BigDecimal merchantDeliveryFeeShare;

    /** 平台收益 */
    @Column(name = "platform_earning", precision = 10, scale = 2)
    private BigDecimal platformEarning;

    /** 结算状态 */
    @Column(name = "settlement_status", length = 20)
    private String settlementStatus;

    /** 结算时间 */
    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    /** 承运类型：courier（平台配送员）/ merchant（商家自配），见 FulfillmentConstants */
    @Column(name = "carrier_type", length = 20)
    private String carrierType;

    /** 配送完成凭证图片URL列表（JSON） */
    @Column(name = "proof_image_urls")
    private String proofImageUrls;

    /** 配送完成备注 */
    @Column(name = "complete_remark", length = 200)
    private String completeRemark;
}
