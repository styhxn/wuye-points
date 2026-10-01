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
@Table(name = "distributor_products")
@EqualsAndHashCode(callSuper = true)
public class DistributorProduct extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "distributor_merchant_id", length = 32, nullable = false)
    private String distributorMerchantId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "wholesale_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal wholesalePrice;

    @Column(name = "suggested_retail_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal suggestedRetailPrice;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "status", length = 20)
    private String status;

    /** 货源类目（如：食品饮料、日用百货），对外橱窗按此筛选项展示与过滤 */
    @Column(name = "category", length = 50)
    private String category;

    /** 收费周期: one_time/monthly/quarterly/yearly */
    @Column(name = "billing_cycle", length = 20)
    private String billingCycle;

    /** 服务区间 city(本市)/community(小区) */
    @Column(name = "service_scope", length = 20)
    private String serviceScope;
}
