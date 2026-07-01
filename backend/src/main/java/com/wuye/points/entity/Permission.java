package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限池表
 * 对应 schema.sql 中 permissions 表（id 前缀 perm_）
 */
@Data
@Entity
@Table(name = "permissions")
@EqualsAndHashCode(callSuper = true)
public class Permission extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "module", length = 50)
    private String module;

    @Column(name = "status", length = 20)
    private String status;
}
