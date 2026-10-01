package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 个体负责人申请表
 * 住户自主申请成为个体负责人（对应首页「一级代理申请」入口），由管理端审核。
 * 审核通过后创建 IndividualLeader 记录并变更住户角色。
 */
@Data
@Entity
@Table(name = "individual_leader_applications")
@EqualsAndHashCode(callSuper = true)
public class IndividualLeaderApplication extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 申请人住户ID */
    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    /** 物业公司ID（冗余，便于查询） */
    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    /** 申请板块 */
    @Column(name = "sector", length = 50, nullable = false)
    private String sector;

    /** 自我介绍/备注 */
    @Column(name = "remark", length = 200)
    private String remark;

    /** pending/approved/rejected */
    @Column(name = "audit_status", length = 20, nullable = false)
    private String auditStatus;

    /** 审核拒绝原因（reject时填写） */
    @Column(name = "reject_reason", length = 200)
    private String rejectReason;

    /** 审核通过时创建的个体负责人ID */
    @Column(name = "individual_leader_id", length = 32)
    private String individualLeaderId;

    /** 审核时间 */
    @Column(name = "audited_at")
    private java.time.LocalDateTime auditedAt;
}
