package com.wuye.points.repository;

import com.wuye.points.entity.PointPoolRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 积分池进出记录 Repository
 */
@Repository
public interface PointPoolRecordRepository extends JpaRepository<PointPoolRecord, String>,
        JpaSpecificationExecutor<PointPoolRecord> {

    Page<PointPoolRecord> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);
}
