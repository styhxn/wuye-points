package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 预设-权限关联表
 * 对应 schema.sql 中 role_preset_permissions 表（id 前缀 rpp_）
 */
@Data
@Entity
@Table(name = "role_preset_permissions")
@EqualsAndHashCode(callSuper = true)
public class RolePresetPermission extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "preset_id", length = 32, nullable = false)
    private String presetId;

    @Column(name = "permission_id", length = 32, nullable = false)
    private String permissionId;
}
