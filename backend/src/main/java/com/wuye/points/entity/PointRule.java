package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 积分规则表 prr_xxxxxxxx
 */
@Data
@Entity
@Table(name = "point_rules")
@EqualsAndHashCode(callSuper = true)
public class PointRule extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "point_type", length = 30, nullable = false)
    private String pointType;

    @Column(name = "source", length = 30, nullable = false)
    private String source;

    @Column(name = "rate", precision = 10, scale = 4)
    private BigDecimal rate;

    @Column(name = "min_amount", precision = 10, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 10, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "min_points")
    private Integer minPoints;

    @Column(name = "max_points")
    private Integer maxPoints;

    @Column(name = "daily_limit")
    private Integer dailyLimit;

    @Column(name = "monthly_limit")
    private Integer monthlyLimit;

    @Column(name = "expire_days")
    private Integer expireDays;

    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;

    @Column(name = "status", length = 20)
    private String status;
}
