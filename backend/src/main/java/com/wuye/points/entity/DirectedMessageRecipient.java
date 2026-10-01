package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "directed_message_recipients")
@EqualsAndHashCode(callSuper = true)
public class DirectedMessageRecipient extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "task_id", length = 32, nullable = false)
    private String taskId;

    @Column(name = "recipient_id", length = 32, nullable = false)
    private String recipientId;

    @Column(name = "message_id", length = 32)
    private String messageId;

    @Column(name = "read_status", length = 20)
    private String readStatus;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}
