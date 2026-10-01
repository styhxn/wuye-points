package com.wuye.points.repository;

import com.wuye.points.entity.CommunityPostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityPostLikeRepository extends JpaRepository<CommunityPostLike, String> {

    boolean existsByPostIdAndResidentId(String postId, String residentId);

    /** 返回实际删除行数（并发取消点赞时可能为 0，调用方据此决定是否减计数） */
    long deleteByPostIdAndResidentId(String postId, String residentId);

    /** 原子插入点赞（唯一键冲突时静默忽略），返回 1=插入成功 / 0=已存在 */
    @Modifying
    @Query(value = "INSERT IGNORE INTO community_post_likes (id, post_id, resident_id) VALUES (:id, :postId, :residentId)",
            nativeQuery = true)
    int insertIgnore(@Param("id") String id, @Param("postId") String postId, @Param("residentId") String residentId);

    List<CommunityPostLike> findByPostIdInAndResidentId(List<String> postIds, String residentId);
}
