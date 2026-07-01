package com.wuye.points.repository;

import com.wuye.points.entity.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户权限 Repository
 * 注：resident_id 已隐含 propertyCompanyId（用户归属唯一物业）
 * 表字段 resident_id 对应实体属性 residentId
 */
@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, String> {

    /**
     * 按用户ID查询权限覆盖项（任务签名 findByUserId，实际字段 residentId）
     */
    @Query("SELECT u FROM UserPermission u WHERE u.residentId = :userId")
    List<UserPermission> findByUserId(@Param("userId") String userId);

    List<UserPermission> findByResidentIdAndPropertyCompanyId(String residentId, String propertyCompanyId);

    Optional<UserPermission> findByResidentIdAndPermissionId(String residentId, String permissionId);
}
