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
 * 公告表
 * 对应 schema.sql 中 announcements 表（id 前缀 ann_）
 */
@Data
@Entity
@Table(name = "announcements")
@EqualsAndHashCode(callSuper = true)
public class Announcement extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "announcement_type", length = 30, nullable = false)
    private String announcementType;

    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;

    @Column(name = "community_id", length = 32)
    private String communityId;

    @Column(name = "merchant_id", length = 32)
    private String merchantId;

    @Column(name = "publisher_id", length = 32, nullable = false)
    private String publisherId;

    @Column(name = "publisher_role", length = 30, nullable = false)
    private String publisherRole;

    @Column(name = "cover_urls", columnDefinition = "json")
    private String coverUrls;

    @Column(name = "target_roles", columnDefinition = "json")
    private String targetRoles;

    @Column(name = "target_buildings", columnDefinition = "json")
    private String targetBuildings;

    @Column(name = "collect_enabled")
    private Boolean collectEnabled;

    @Column(name = "collect_fields", columnDefinition = "json")
    private String collectFields;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
}
