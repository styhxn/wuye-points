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
 * 欠费催缴预览快照（持久化）。
 * 由 ArrearsReminderStateStore 写入，发送时按 token 消费，过期后由定时清理。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "arrears_reminder_preview")
public class ArrearsReminderPreview {

    @Id
    @Column(name = "token", length = 64)
    private String token;

    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;

    @Column(name = "community_id", length = 32)
    private String communityId;

    @Column(name = "building", length = 50)
    private String building;

    @Column(name = "fee_type", length = 30)
    private String feeType;

    /** 收件人住户ID，JSON 数组字符串存储 */
    @Column(name = "resident_ids", columnDefinition = "text")
    private String residentIds;

    @Column(name = "recipient_count")
    private int recipientCount;

    @Column(name = "total_arrears_amount", precision = 12, scale = 2)
    private BigDecimal totalArrearsAmount;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
