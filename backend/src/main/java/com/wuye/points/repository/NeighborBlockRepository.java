package com.wuye.points.repository;

import com.wuye.points.entity.NeighborBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 邻居屏蔽记录 Repository
 */
@Repository
public interface NeighborBlockRepository extends JpaRepository<NeighborBlock, String> {

    Optional<NeighborBlock> findByBlockerIdAndBlockedId(String blockerId, String blockedId);

    boolean existsByBlockerIdAndBlockedId(String blockerId, String blockedId);

    boolean existsByBlockedIdAndBlockerId(String blockedId, String blockerId);
}
