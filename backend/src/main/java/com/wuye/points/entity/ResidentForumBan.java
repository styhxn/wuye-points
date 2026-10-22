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
 * 社区论坛禁言记录表（影子封禁）。
 *
 * <p>物业公司可禁止某住户在社区论坛发帖/评论。被禁用户<b>无感知</b>：
 * 发帖/评论接口照常成功、本人可见，但其他住户与管理端以外的任何人均看不到其内容
 * （查询层按 {@code community_posts.shadow_banned} / {@code community_comments.shadow_banned}
 * 与作者可见性过滤）。</p>
 *
 * <p>禁言范围按物业公司管辖；{@code expiresAt} 为空表示永久禁言，到期由调度器自动解除
 * （恢复内容可见并回补评论计数）。</p>
 */
@Data
@Entity
@Table(name = "resident_forum_bans")
@EqualsAndHashCode(callSuper = true)
public class ResidentForumBan extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "community_id", length = 32)
    private String communityId;

    @Column(name = "reason", length = 200)
    private String reason;

    @Column(name = "banned_by", length = 32)
    private String bannedBy;

    @Column(name = "banned_at")
    private LocalDateTime bannedAt;

    /** 到期时间；NULL=永久禁言 */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /** active=生效中 / lifted=管理员解除 / expired=到期自动解除 */
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "lifted_by", length = 32)
    private String liftedBy;

    @Column(name = "lifted_at")
    private LocalDateTime liftedAt;
}
