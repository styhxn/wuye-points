package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "resident_merchant_listings")
@EqualsAndHashCode(callSuper = true)
public class ResidentMerchantListing extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_merchant_id", length = 32, nullable = false)
    private String residentMerchantId;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "distributor_product_id", length = 32, nullable = false)
    private String distributorProductId;

    @Column(name = "retail_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal retailPrice;

    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "share_count")
    private Integer shareCount;

    /** 拿货件数（从一级代理库存一次性扣减） */
    @Column(name = "took_quantity", nullable = false)
    private Integer tookQuantity;

    /** 已卖出件数（订单完成结算时累加） */
    @Column(name = "sold_quantity", nullable = false)
    private Integer soldQuantity;

    /** 是否因卖完自动下架（退款恢复余量后可自动重新上架） */
    @Column(name = "sold_out_unlisted", nullable = false)
    private Boolean soldOutUnlisted;

    /** 可售件数（未锁定未售出；买家下单锁定时扣减，取消/退款恢复） */
    @Column(name = "quantity_remaining", nullable = false)
    private Integer quantityRemaining;
}
