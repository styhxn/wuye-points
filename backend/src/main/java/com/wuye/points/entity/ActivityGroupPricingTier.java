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
 * 活动组多课时价格表
 * 对应 schema.sql 中 activity_group_pricing_tiers 表（id 前缀 agt_）
 */
@Data
@Entity
@Table(name = "activity_group_pricing_tiers")
@EqualsAndHashCode(callSuper = true)
public class ActivityGroupPricingTier extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "activity_group_id", length = 32, nullable = false)
    private String activityGroupId;

    @Column(name = "tier_name", length = 50, nullable = false)
    private String tierName;

    @Column(name = "tier_code", length = 30, nullable = false)
    private String tierCode;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "period", length = 20, nullable = false)
    private String period;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "status", length = 20)
    private String status;
}
