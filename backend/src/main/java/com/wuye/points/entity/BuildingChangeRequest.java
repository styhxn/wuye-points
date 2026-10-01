package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 楼栋变更申请表（住户提交 → 物业审批 → 通过后写入 residents 地址）
 */
@Data
@Entity
@Table(name = "building_change_requests")
@EqualsAndHashCode(callSuper = true)
public class BuildingChangeRequest extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 申请住户ID */
    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;

    @Column(name = "community_id", length = 32)
    private String communityId;

    @Column(name = "building", length = 20)
    private String building;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "floor", length = 20)
    private String floor;

    @Column(name = "room", length = 20)
    private String room;

    /** pending/approved/rejected（兼容 pending_audit） */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "reject_reason", length = 200)
    private String rejectReason;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Column(name = "audited_at")
    private LocalDateTime auditedAt;

    @Column(name = "auditor_id", length = 32)
    private String auditorId;
}
