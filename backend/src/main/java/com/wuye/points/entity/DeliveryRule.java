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
 * 配送规则表
 * 对应 schema.sql 中 delivery_rules 表（id 前缀 drl_，与分成记录 dr_ 区分）
 */
@Data
@Entity
@Table(name = "delivery_rules")
@EqualsAndHashCode(callSuper = true)
public class DeliveryRule extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "base_fee", precision = 10, scale = 2, nullable = false)
    private BigDecimal baseFee;

    @Column(name = "per_kg_fee", precision = 10, scale = 2)
    private BigDecimal perKgFee;

    @Column(name = "free_threshold", precision = 10, scale = 2)
    private BigDecimal freeThreshold;

    @Column(name = "max_weight", precision = 10, scale = 2)
    private BigDecimal maxWeight;

    @Column(name = "max_distance", precision = 10, scale = 2)
    private BigDecimal maxDistance;

    @Column(name = "courier_share_rate", precision = 5, scale = 4)
    private BigDecimal courierShareRate;

    @Column(name = "courier_per_order", precision = 10, scale = 2)
    private BigDecimal courierPerOrder;

    @Column(name = "timeout_minutes")
    private Integer timeoutMinutes;

    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;

    @Column(name = "status", length = 20)
    private String status;
}
