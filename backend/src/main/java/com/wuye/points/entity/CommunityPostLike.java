package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 社区论坛点赞表（独立表，替代 community_posts.liked_by JSON，避免并发丢更新）
 * 对应 community_post_likes 表（id 前缀 like_），唯一键 (post_id, resident_id) 防重复点赞
 */
@Data
@Entity
@Table(name = "community_post_likes", uniqueConstraints = @UniqueConstraint(
        name = "uk_post_resident",
        columnNames = {"post_id", "resident_id"}))
@EqualsAndHashCode(callSuper = true)
public class CommunityPostLike extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "post_id", length = 32, nullable = false)
    private String postId;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;
}
