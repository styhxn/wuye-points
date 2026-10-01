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
 * 区域负责人表（id 前缀 rl_）
 * 对应 schema.sql 中 regional_leaders 表
 */
@Data
@Entity
@Table(name = "regional_leaders")
@EqualsAndHashCode(callSuper = true)
public class RegionalLeader extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    /** 所属板块负责人 */
    @Column(name = "sector_leader_id", length = 32)
    private String sectorLeaderId;

    /** 所属统筹（也可由统筹直接任命） */
    @Column(name = "coordinator_id", length = 32)
    private String coordinatorId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    /** 负责区域名称 */
    @Column(name = "region_name", length = 100)
    private String regionName;

    /** 从板块负责人中抽取的分成比例 */
    @Column(name = "share_rate", precision = 5, scale = 4)
    private BigDecimal shareRate;

    /** active/inactive */
    @Column(name = "status", length = 20)
    private String status;
}
