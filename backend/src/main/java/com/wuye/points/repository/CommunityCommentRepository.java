package com.wuye.points.repository;

import com.wuye.points.entity.CommunityComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, String>,
        JpaSpecificationExecutor<CommunityComment> {

    /** 影子封禁打标/取消：批量更新某作者全部评论 */
    @Modifying
    @Query("update CommunityComment c set c.shadowBanned = :flag where c.authorId = :authorId")
    int updateShadowBannedByAuthor(@Param("authorId") String authorId, @Param("flag") boolean flag);
}
