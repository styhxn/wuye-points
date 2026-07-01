package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 物业费规则表 pfr_xxxxxxxx
 */
@Data
@Entity
@Table(name = "property_fee_rules")
@EqualsAndHashCode(callSuper = true)
public class PropertyFeeRule extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "fee_type", length = 30, nullable = false)
    private String feeType;

    @Column(name = "calculation_method", length = 30, nullable = false)
    private String calculationMethod;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "billing_cycle", length = 20, nullable = false)
    private String billingCycle;

    @Column(name = "auto_deduct_enabled")
    private Boolean autoDeductEnabled;

    @Column(name = "overdue_rate", precision = 5, scale = 4)
    private BigDecimal overdueRate;

    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;

    @Column(name = "status", length = 20)
    private String status;
}
