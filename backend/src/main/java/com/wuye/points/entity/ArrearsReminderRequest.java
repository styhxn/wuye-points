package com.wuye.points.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 欠费催缴发送幂等记录（持久化）。
 * request_id 为主键，重复插入会因唯一约束失败，从而实现幂等。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "arrears_reminder_request")
public class ArrearsReminderRequest {

    @Id
    @Column(name = "request_id", length = 64)
    private String requestId;

    @Column(name = "batch_id", length = 64)
    private String batchId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
