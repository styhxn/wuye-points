package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户权限表（覆盖预设：先加载预设，再用此表增减单项）
 * 对应 schema.sql 中 user_permissions 表（id 前缀 up_）
 */
@Data
@Entity
@Table(name = "user_permissions")
@EqualsAndHashCode(callSuper = true)
public class UserPermission extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "permission_id", length = 32, nullable = false)
    private String permissionId;

    @Column(name = "granted", nullable = false)
    private Boolean granted;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "operator_id", length = 32, nullable = false)
    private String operatorId;
}
