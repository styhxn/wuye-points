package com.wuye.points.repository;

import com.wuye.points.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 操作日志 Repository
 * 注：按规则仅按操作者过滤；同时提供按物业过滤维度便于审计
 */
@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, String>,
        JpaSpecificationExecutor<OperationLog> {

    Page<OperationLog> findByOperatorId(String operatorId, Pageable pageable);

    Page<OperationLog> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);
}
