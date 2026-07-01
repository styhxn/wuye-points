package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限变更日志表
 * 对应 schema.sql 中 permission_change_logs 表（id 前缀 pcl_）
 */
@Data
@Entity
@Table(name = "permission_change_logs")
@EqualsAndHashCode(callSuper = true)
public class PermissionChangeLog extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "operator_id", length = 32, nullable = false)
    private String operatorId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "action", length = 20, nullable = false)
    private String action;

    @Column(name = "permission_code", length = 50)
    private String permissionCode;

    @Column(name = "preset_code", length = 50)
    private String presetCode;

    @Column(name = "before_value", length = 200)
    private String beforeValue;

    @Column(name = "after_value", length = 200)
    private String afterValue;
}
