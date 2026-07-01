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
 * 个体负责人表
 * 对应 schema.sql 中 individual_leaders 表（id 前缀 il_）
 */
@Data
@Entity
@Table(name = "individual_leaders")
@EqualsAndHashCode(callSuper = true)
public class IndividualLeader extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "sector_leader_id", length = 32, nullable = false)
    private String sectorLeaderId;

    @Column(name = "sector", length = 50, nullable = false)
    private String sector;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "commission_rate", precision = 5, scale = 4)
    private BigDecimal commissionRate;

    @Column(name = "status", length = 20)
    private String status;
}
