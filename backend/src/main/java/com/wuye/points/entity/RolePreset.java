package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色快捷预设表
 * 对应 schema.sql 中 role_presets 表（id 前缀 rp_）
 */
@Data
@Entity
@Table(name = "role_presets")
@EqualsAndHashCode(callSuper = true)
public class RolePreset extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;

    @Column(name = "status", length = 20)
    private String status;
}
