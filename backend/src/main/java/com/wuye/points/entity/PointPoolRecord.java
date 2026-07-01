package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 积分池进出记录表 ppr_xxxxxxxx
 */
@Data
@Entity
@Table(name = "point_pool_records")
@EqualsAndHashCode(callSuper = true)
public class PointPoolRecord extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "pool_type", length = 30, nullable = false)
    private String poolType;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal balance;

    @Column(name = "source", length = 30)
    private String source;

    @Column(name = "source_id", length = 32)
    private String sourceId;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "operator_id", length = 32)
    private String operatorId;
}
