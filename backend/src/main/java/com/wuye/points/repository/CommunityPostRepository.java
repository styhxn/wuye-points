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
}
