package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 内容举报表
 * 对应 content_reports 表（id 前缀 crp_）
 * 同一用户对同一内容只能举报一次
 */
@Data
@Entity
@Table(name = "content_reports", uniqueConstraints = @UniqueConstraint(
        name = "uk_reporter_target",
        columnNames = {"reporter_id", "target_type", "target_id"}))
@EqualsAndHashCode(callSuper = true)
public class ContentReport extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "reporter_id", length = 32, nullable = false)
    private String reporterId;

    @Column(name = "reporter_name", length = 100)
    private String reporterName;

    /** post / comment */
    @Column(name = "target_type", length = 20, nullable = false)
    private String targetType;

    @Column(name = "target_id", length = 32, nullable = false)
    private String targetId;

    @Column(name = "reason_type", length = 30)
    private String reasonType;

    @Column(name = "reason_detail", length = 500)
    private String reasonDetail;

    /** pending / accepted / rejected */
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "community_id", length = 32)
    private String communityId;

    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;

    @Column(name = "handler_id", length = 32)
    private String handlerId;

    @Column(name = "handler_name", length = 100)
    private String handlerName;

    @Column(name = "handle_remark", length = 500)
    private String handleRemark;

    @Column(name = "handled_at")
    private LocalDateTime handledAt;
}
