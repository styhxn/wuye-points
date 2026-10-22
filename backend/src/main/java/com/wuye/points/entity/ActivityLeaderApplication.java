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
 * 活动组组长申请表（住户自主申请成为活动组组长，管理端审核）。
 *
 * <p>审核通过后创建 {@link ActivityLeaderIdentity} 身份记录并自动开通组长小店
 * （merchant_source=group_leader），与个体负责人申请（individual_leader_applications）、
 * 一级代理申请（distributor_applications）同模式。</p>
 */
@Data
@Entity
@Table(name = "activity_leader_applications")
@EqualsAndHashCode(callSuper = true)
public class ActivityLeaderApplication extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "community_id", length = 32)
    private String communityId;

    /** 擅长领域/活动类型（可选，如 文艺/体育/亲子/户外） */
    @Column(name = "activity_field", length = 50)
    private String activityField;

    /** 自我介绍/备注 */
    @Column(name = "remark", length = 200)
    private String remark;

    /** pending / approved / rejected */
    @Column(name = "audit_status", length = 20)
    private String auditStatus;

    @Column(name = "reject_reason", length = 200)
    private String rejectReason;

    /** 审核通过后创建的组长身份记录ID */
    @Column(name = "activity_leader_identity_id", length = 32)
    private String activityLeaderIdentityId;

    @Column(name = "audited_by", length = 32)
    private String auditedBy;

    @Column(name = "audited_at")
    private LocalDateTime auditedAt;
}
