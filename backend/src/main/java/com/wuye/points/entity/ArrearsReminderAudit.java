package com.wuye.points.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 欠费催缴发送审计 / 批次结果（持久化）。
 * 每次成功发送产生一条记录，用于追溯与统计。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "arrears_reminder_audit")
public class ArrearsReminderAudit {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "batch_id", length = 64)
    private String batchId;

    @Column(name = "request_id", length = 64)
    private String requestId;

    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;

    @Column(name = "community_id", length = 32)
    private String communityId;

    @Column(name = "building", length = 50)
    private String building;

    @Column(name = "fee_type", length = 30)
    private String feeType;

    @Column(name = "channels", length = 50)
    private String channels;

    @Column(name = "recipient_count")
    private int recipientCount;

    @Column(name = "notified_count")
    private int notifiedCount;

    @Column(name = "skipped_count")
    private int skippedCount;

    @Column(name = "failed_count")
    private int failedCount;

    @Column(name = "in_app_success")
    private int inAppSuccess;

    @Column(name = "in_app_failed")
    private int inAppFailed;

    @Column(name = "wechat_success")
    private int wechatSuccess;

    @Column(name = "wechat_skipped")
    private int wechatSkipped;

    @Column(name = "wechat_failed")
    private int wechatFailed;

    @Column(name = "total_arrears_amount", precision = 12, scale = 2)
    private BigDecimal totalArrearsAmount;

    @Column(name = "operator", length = 64)
    private String operator;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
