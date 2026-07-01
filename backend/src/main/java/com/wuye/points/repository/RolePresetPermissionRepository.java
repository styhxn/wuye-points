package com.wuye.points.repository;

import com.wuye.points.entity.RolePresetPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 预设-权限关联 Repository
 * 注：预设权限关联为全局，无需 propertyCompanyId 过滤
 */
@Repository
public interface RolePresetPermissionRepository extends JpaRepository<RolePresetPermission, String> {

    List<RolePresetPermission> findByPresetId(String presetId);
}
