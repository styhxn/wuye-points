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
 * 商家积分购买表
 * 对应 schema.sql 中 merchant_point_purchases 表（id 前缀 mpp_）
 */
@Data
@Entity
@Table(name = "merchant_point_purchases")
@EqualsAndHashCode(callSuper = true)
public class MerchantPointPurchase extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "point_amount", nullable = false)
    private Integer pointAmount;

    @Column(name = "pay_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal payAmount;

    @Column(name = "audit_status", length = 20)
    private String auditStatus;

    @Column(name = "audited_at")
    private LocalDateTime auditedAt;

    @Column(name = "auditor_id", length = 32)
    private String auditorId;

    @Column(name = "remaining_points")
    private Integer remainingPoints;
}
