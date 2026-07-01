package com.wuye.points.repository;

import com.wuye.points.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 权限池 Repository
 * 注：权限池为全局共享，无需 propertyCompanyId 过滤
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {

    List<Permission> findByModule(String module);

    Optional<Permission> findByCode(String code);
}
