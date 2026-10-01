package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 社区论坛帖子表
 * 对应 community_posts 表（id 前缀 cp_）
 * 可见范围为发帖人所在小区（community_id）
 */
@Data
@Entity
@Table(name = "community_posts")
@EqualsAndHashCode(callSuper = true)
public class CommunityPost extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "community_id", length = 32, nullable = false)
    private String communityId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "author_id", length = 32, nullable = false)
    private String authorId;

    @Column(name = "author_name", length = 100)
    private String authorName;

    @Column(name = "author_role", length = 30)
    private String authorRole;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "image_urls", columnDefinition = "json")
    private String imageUrls;

    /** published / hidden / deleted */
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "comment_count")
    private Integer commentCount = 0;

    /** 可见范围：community=本小区可见（默认）；family=仅指定住户可见 */
    @Column(name = "visibility_type", length = 20)
    private String visibilityType;

    /** 当 visibilityType=family 时，可见的指定住户ID */
    @Column(name = "visible_resident_id", length = 32)
    private String visibleResidentId;
}
