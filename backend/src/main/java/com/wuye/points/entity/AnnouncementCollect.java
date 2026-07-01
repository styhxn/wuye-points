package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公告信息收集表
 * 对应 schema.sql 中 announcement_collects 表（id 前缀 ac_）
 */
@Data
@Entity
@Table(name = "announcement_collects")
@EqualsAndHashCode(callSuper = true)
public class AnnouncementCollect extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "announcement_id", length = 32, nullable = false)
    private String announcementId;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "collected_data", nullable = false, columnDefinition = "json")
    private String collectedData;
}
