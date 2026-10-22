package com.wuye.points.repository;

import com.wuye.points.entity.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, String>,
        JpaSpecificationExecutor<CommunityPost> {

    /** 原子更新点赞数（GREATEST 防负数、COALESCE 兼容历史 NULL），避免并发读改写丢更新 */
    @Modifying
    @Query(value = "UPDATE community_posts SET like_count = GREATEST(0, COALESCE(like_count, 0) + :delta) WHERE id = :postId",
            nativeQuery = true)
    int updateLikeCount(@Param("postId") String postId, @Param("delta") int delta);

    /** 影子封禁打标/取消：批量更新某作者全部帖子 */
    @Modifying
    @Query("update CommunityPost p set p.shadowBanned = :flag where p.authorId = :authorId")
    int updateShadowBannedByAuthor(@Param("authorId") String authorId, @Param("flag") boolean flag);

    /**
     * 禁言生效：把该作者「此前参与计数」的评论（published 且未打标）从各帖 comment_count 中扣回。
     * 先执行本扣减、再执行打标，保证计数与可见性一致（GREATEST 防负数）。
     */
    @Modifying
    @Query(value = "UPDATE community_posts p JOIN (SELECT post_id, COUNT(*) cnt FROM community_comments "
            + "WHERE author_id = :authorId AND status = 'published' AND shadow_banned = 0 GROUP BY post_id) c "
            + "ON c.post_id = p.id SET p.comment_count = GREATEST(0, COALESCE(p.comment_count, 0) - c.cnt)",
            nativeQuery = true)
    int decrementCommentCountForBannedAuthor(@Param("authorId") String authorId);

    /**
     * 禁言解除：把该作者「曾被隐藏」的评论（published 且已打标）加回各帖 comment_count。
     * 先执行本加回、再执行取消打标。
     */
    @Modifying
    @Query(value = "UPDATE community_posts p JOIN (SELECT post_id, COUNT(*) cnt FROM community_comments "
            + "WHERE author_id = :authorId AND status = 'published' AND shadow_banned = 1 GROUP BY post_id) c "
            + "ON c.post_id = p.id SET p.comment_count = COALESCE(p.comment_count, 0) + c.cnt",
            nativeQuery = true)
    int incrementCommentCountForUnbannedAuthor(@Param("authorId") String authorId);
}
