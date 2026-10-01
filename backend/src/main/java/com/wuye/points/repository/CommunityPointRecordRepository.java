package com.wuye.points.repository;

import com.wuye.points.entity.CommunityPointRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 小区建设积分流水 Repository
 */
@Repository
public interface CommunityPointRecordRepository extends JpaRepository<CommunityPointRecord, String> {

    Page<CommunityPointRecord> findByCommunityIdOrderByCreatedAtDesc(String communityId, Pageable pageable);
}
