package com.wuye.points.repository;

import com.wuye.points.entity.PointPoolRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 积分池进出记录 Repository
 * 注：point_pool_id 已隐含 propertyCompanyId（积分池按物业维度唯一）
 */
@Repository
public interface PointPoolRecordRepository extends JpaRepository<PointPoolRecord, String>,
        JpaSpecificationExecutor<PointPoolRecord> {

    List<PointPoolRecord> findByPointPoolId(String pointPoolId);

    Page<PointPoolRecord> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);
}
