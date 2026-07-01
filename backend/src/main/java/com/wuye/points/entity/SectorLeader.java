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
 * 板块负责人表
 * 对应 schema.sql 中 sector_leaders 表（id 前缀 sl_）
 */
@Data
@Entity
@Table(name = "sector_leaders")
@EqualsAndHashCode(callSuper = true)
public class SectorLeader extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "sector", length = 50, nullable = false)
    private String sector;

    @Column(name = "commission_rate", precision = 5, scale = 4)
    private BigDecimal commissionRate;

    @Column(name = "coordinator_id", length = 32)
    private String coordinatorId;

    @Column(name = "status", length = 20)
    private String status;
}
