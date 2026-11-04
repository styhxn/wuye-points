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

    /** 内容类型：notification（普通公告）/ product（产品推荐）/ activity */
    @Column(name = "content_type", length = 30)
    private String contentType = "notification";

    /** 商品ID，仅 contentType=product 时有值（产品推荐公告） */
    @Column(name = "product_id", length = 32)
    private String productId;

    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;

    @Column(name = "community_id", length = 32)
    private String communityId;

    @Column(name = "merchant_id", length = 32)
    private String merchantId;

    @Column(name = "activity_group_id", length = 32)
    private String activityGroupId;

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

    /**
     * 定向单元列表（JSON 数组文本）。存 varchar 而非 json：单元可见性靠 LIKE 判定，
     * H2 会把 json 列读成二次编码字符串导致该路径无法测试，详见 V37 迁移脚本说明。
     */
    @Column(name = "target_units", length = 500)
    private String targetUnits;

    @Column(name = "collect_enabled")
    private Boolean collectEnabled;

    @Column(name = "collect_fields", columnDefinition = "json")
    private String collectFields;

    @Column(name = "status", length = 20)
    private String status;

    /**
     * 投放渠道：announcement_board（仅公告栏）/ chat（站内消息）/ both。
     * 生产列带 NOT NULL，而 Hibernate 插入时会列出全部映射列，故必须有 Java 侧默认值。
     */
    @Column(name = "delivery_channel", length = 30, nullable = false)
    private String deliveryChannel = "announcement_board";

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
}
