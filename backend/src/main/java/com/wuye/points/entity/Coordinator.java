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
 * 统筹表
 * 对应 schema.sql 中 coordinators 表（id 前缀 co_）
 */
@Data
@Entity
@Table(name = "coordinators")
@EqualsAndHashCode(callSuper = true)
public class Coordinator extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "commission_rate", precision = 5, scale = 4)
    private BigDecimal commissionRate;

    /** 统筹描述/备注 */
    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "status", length = 20)
    private String status;
}
