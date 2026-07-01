package com.wuye.points.repository;

import com.wuye.points.entity.PermissionChangeLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 权限变更日志 Repository
 * 注：仅按操作者/目标用户过滤（任务规则）
 * 表字段 resident_id（被操作用户）对应实体属性 residentId
 * 表字段 operator_id 对应实体属性 operatorId
 */
@Repository
public interface PermissionChangeLogRepository extends JpaRepository<PermissionChangeLog, String>,
        JpaSpecificationExecutor<PermissionChangeLog> {

    Page<PermissionChangeLog> findByOperatorId(String operatorId, Pageable pageable);

    /**
     * 按目标用户（被操作用户）查询，任务签名 findByTargetUserId，实际字段 residentId
     */
    @Query("SELECT p FROM PermissionChangeLog p WHERE p.residentId = :targetUserId")
    Page<PermissionChangeLog> findByTargetUserId(@Param("targetUserId") String targetUserId, Pageable pageable);
}
