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
 * 活动组成员表
 * 对应 schema.sql 中 activity_group_members 表（id 前缀 agm_）
 */
@Data
@Entity
@Table(name = "activity_group_members")
@EqualsAndHashCode(callSuper = true)
public class ActivityGroupMember extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "group_id", length = 32, nullable = false)
    private String groupId;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "role", length = 20)
    private String role;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "status", length = 20)
    private String status;

    /** 组员个人简介（姓名、年龄、擅长领域等，JSON 字符串） */
    @Column(name = "introduction", columnDefinition = "text")
    private String introduction;
}
