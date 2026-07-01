package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志表
 * 对应 schema.sql 中 operation_logs 表（id 前缀 ol_）
 */
@Data
@Entity
@Table(name = "operation_logs")
@EqualsAndHashCode(callSuper = true)
public class OperationLog extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "operator_id", length = 32, nullable = false)
    private String operatorId;

    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;

    @Column(name = "module", length = 50, nullable = false)
    private String module;

    @Column(name = "action", length = 50, nullable = false)
    private String action;

    @Column(name = "target_type", length = 50)
    private String targetType;

    @Column(name = "target_id", length = 32)
    private String targetId;

    @Column(name = "before_data", columnDefinition = "json")
    private String beforeData;

    @Column(name = "after_data", columnDefinition = "json")
    private String afterData;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;
}
