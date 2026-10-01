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

@Data
@Entity
@Table(name = "resident_merchant_settlements")
@EqualsAndHashCode(callSuper = true)
public class ResidentMerchantSettlement extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "order_id", length = 32, nullable = false)
    private String orderId;

    @Column(name = "listing_id", length = 32, nullable = false)
    private String listingId;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "distributor_merchant_id", length = 32, nullable = false)
    private String distributorMerchantId;

    @Column(name = "order_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal orderAmount;

    @Column(name = "wholesale_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal wholesaleAmount;

    @Column(name = "reseller_profit", precision = 10, scale = 2, nullable = false)
    private BigDecimal resellerProfit;

    @Column(name = "platform_commission", precision = 10, scale = 2)
    private BigDecimal platformCommission;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "settle_at")
    private LocalDateTime settleAt;
}
