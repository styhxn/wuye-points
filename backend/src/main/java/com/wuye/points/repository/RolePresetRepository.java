package com.wuye.points.repository;

import com.wuye.points.entity.RolePreset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色快捷预设 Repository
 * 注：预设为全局/平台级，无需 propertyCompanyId 过滤
 */
@Repository
public interface RolePresetRepository extends JpaRepository<RolePreset, String> {

    Optional<RolePreset> findByCode(String code);

    List<RolePreset> findByPropertyCompanyIdIsNull();

    List<RolePreset> findByPropertyCompanyId(String propertyCompanyId);

    List<RolePreset> findByPropertyCompanyIdIsNotNull();
}
