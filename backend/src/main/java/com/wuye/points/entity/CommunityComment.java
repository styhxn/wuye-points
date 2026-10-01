package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 社区论坛评论表（支持二级回复）
 * 对应 community_comments 表（id 前缀 cc_）
 */
@Data
@Entity
@Table(name = "community_comments")
@EqualsAndHashCode(callSuper = true)
public class CommunityComment extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "post_id", length = 32, nullable = false)
    private String postId;

    @Column(name = "author_id", length = 32, nullable = false)
    private String authorId;

    @Column(name = "author_name", length = 100)
    private String authorName;

    @Column(name = "author_role", length = 30)
    private String authorRole;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    /** 被回复的评论ID（二级回复时填写，顶级评论为 null） */
    @Column(name = "parent_id", length = 32)
    private String parentId;

    /** 被回复用户ID */
    @Column(name = "reply_to_user_id", length = 32)
    private String replyToUserId;

    /** 被回复用户名称 */
    @Column(name = "reply_to_user_name", length = 100)
    private String replyToUserName;

    /** published / deleted */
    @Column(name = "status", length = 20)
    private String status;
}
