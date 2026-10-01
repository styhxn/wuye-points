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
 * 物业公司配送价格区间表
 * 对应 delivery_price_ranges 表（id 前缀 dpr_）
 */
@Data
@Entity
@Table(name = "delivery_price_ranges")
@EqualsAndHashCode(callSuper = true)
public class DeliveryPriceRange extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "min_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal maxPrice;

    @Column(name = "distance_type", length = 20)
    private String distanceType = "any";

    @Column(name = "delivery_scope", length = 20)
    private String deliveryScope = "both";

    @Column(name = "status", length = 20)
    private String status;
}
