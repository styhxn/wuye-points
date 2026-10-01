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
 * 奖励金/积分发放申请
 * 由住户(resident)或商家(merchant)提交，经物业(property_admin)审批通过后向目标住户发放积分。
 * attribution_mode 支持两种归属模式：property(归属物业积分池) / platform(归属平台，由平台资金发放，不从物业池扣减)。
 */
@Data
@Entity
@Table(name = "reward_grant_applications")
@EqualsAndHashCode(callSuper = true)
public class RewardGrantApplication extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 申请人ID（住户或商家 owner 的 residentId） */
    @Column(name = "applicant_id", length = 32, nullable = false)
    private String applicantId;

    /** 申请人角色 resident / merchant */
    @Column(name = "applicant_role", length = 20, nullable = false)
    private String applicantRole;

    /** 目标住户ID（积分实际发放对象） */
    @Column(name = "target_resident_id", length = 32, nullable = false)
    private String targetResidentId;

    /** 目标住户姓名快照（展示用） */
    @Column(name = "target_resident_name", length = 50)
    private String targetResidentName;

    /** 发放类型 points(积分)/reward(奖励金) */
    @Column(name = "grant_type", length = 20, nullable = false)
    private String grantType;

    /** 积分数量 */
    @Column(name = "point_amount", nullable = false)
    private Integer pointAmount;

    /** 归属模式 property / platform */
    @Column(name = "attribution_mode", length = 20, nullable = false)
    private String attributionMode;

    /** 申请理由 */
    @Column(name = "reason", length = 255)
    private String reason;

    /** pending_audit / approved / rejected */
    @Column(name = "audit_status", length = 20, nullable = false)
    private String auditStatus;

    /** 审批人ID */
    @Column(name = "approver_id", length = 32)
    private String approverId;

    /** 审批备注（拒绝原因） */
    @Column(name = "approver_remark", length = 255)
    private String approverRemark;

    /** 审批时间 */
    @Column(name = "audited_at")
    private LocalDateTime auditedAt;

    /** 物业公司ID（冗余，便于查询与数据隔离） */
    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;
}
