package com.wuye.points.repository;

import com.wuye.points.entity.NeighborBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 邻居屏蔽记录 Repository
 */
@Repository
public interface NeighborBlockRepository extends JpaRepository<NeighborBlock, String> {

    Optional<NeighborBlock> findByBlockerIdAndBlockedId(String blockerId, String blockedId);

    boolean existsByBlockerIdAndBlockedId(String blockerId, String blockedId);

    boolean existsByBlockedIdAndBlockerId(String blockedId, String blockerId);

    /** 批量筛出「已屏蔽 blockedId 的住户」（楼长站内分享前的收件人过滤） */
    List<NeighborBlock> findByBlockerIdInAndBlockedId(Collection<String> blockerIds, String blockedId);
}
