package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息表
 * 对应 schema.sql 中 messages 表（id 前缀 msg_）
 */
@Data
@Entity
@Table(name = "messages")
@EqualsAndHashCode(callSuper = true)
public class Message extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "from_id", length = 32, nullable = false)
    private String fromId;

    @Column(name = "to_id", length = 32)
    private String toId;

    @Column(name = "to_role", length = 30)
    private String toRole;

    @Column(name = "message_type", length = 30, nullable = false)
    private String messageType;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "chat_type", length = 20)
    private String chatType;

    @Column(name = "group_id", length = 32)
    private String groupId;

    @Column(name = "read_status", length = 20)
    private String readStatus;

    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;
}
