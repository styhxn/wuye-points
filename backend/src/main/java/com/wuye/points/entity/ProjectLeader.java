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
 * 项目负责人表（id 前缀 pl_）
 * 对应 schema.sql 中 project_leaders 表
 */
@Data
@Entity
@Table(name = "project_leaders")
@EqualsAndHashCode(callSuper = true)
public class ProjectLeader extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "regional_leader_id", length = 32)
    private String regionalLeaderId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "project_name", length = 100)
    private String projectName;

    /** 从区域负责人中抽取的分成比例 */
    @Column(name = "share_rate", precision = 5, scale = 4)
    private BigDecimal shareRate;

    /** active/inactive */
    @Column(name = "status", length = 20)
    private String status;
}
