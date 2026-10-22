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
 * 活动组组长身份表（住户子身份/标签，2026-10-21 组长标签化改造）。
 *
 * <p>与单元楼长（building_leaders）、统筹/板块/个体负责人同模式：
 * {@code residents.role} 保持 {@code resident} 不变，「是否组长」由本表 active 记录判定。
 * 身份来源：{@code appoint}（管理端直接创建账号时任命）/ {@code application}（住户申请审核通过）。</p>
 *
 * <p>兼容存量数据：改造前 role=activity_leader 的账号由迁移脚本批量转换
 * （建 active 身份记录 + role 归一为 resident），JWT 中残留的 activity_leader 角色值
 * 由 {@code LeaderIdentityGuard} 兼容判定。</p>
 */
@Data
@Entity
@Table(name = "activity_leader_identities")
@EqualsAndHashCode(callSuper = true)
public class ActivityLeaderIdentity extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    /** 身份来源：appoint=管理端任命 / application=住户申请审核通过 */
    @Column(name = "source", length = 20)
    private String source;

    /** 来源为 application 时对应的申请记录ID */
    @Column(name = "application_id", length = 32)
    private String applicationId;

    /** active / revoked */
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_by", length = 32)
    private String createdBy;

    @Column(name = "revoked_by", length = 32)
    private String revokedBy;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
}
