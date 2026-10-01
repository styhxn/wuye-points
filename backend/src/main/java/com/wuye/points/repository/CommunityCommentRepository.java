package com.wuye.points.repository;

import com.wuye.points.entity.CommunityComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, String>,
        JpaSpecificationExecutor<CommunityComment> {
}
