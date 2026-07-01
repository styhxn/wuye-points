package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 物业币规则表 pcrr_xxxxxxxx
 */
@Data
@Entity
@Table(name = "property_coin_rules")
@EqualsAndHashCode(callSuper = true)
public class PropertyCoinRule extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "coin_type", length = 30, nullable = false)
    private String coinType;

    @Column(name = "source", length = 30, nullable = false)
    private String source;

    @Column(name = "rate", precision = 10, scale = 4)
    private BigDecimal rate;

    @Column(name = "min_amount", precision = 10, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 10, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "daily_limit", precision = 10, scale = 2)
    private BigDecimal dailyLimit;

    @Column(name = "monthly_limit", precision = 10, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;

    @Column(name = "status", length = 20)
    private String status;
}
