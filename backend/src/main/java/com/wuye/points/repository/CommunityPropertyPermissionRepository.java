package com.wuye.points.repository;

import com.wuye.points.entity.CommunityPropertyPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommunityPropertyPermissionRepository extends JpaRepository<CommunityPropertyPermission, String> {

    Optional<CommunityPropertyPermission> findByBindingId(String bindingId);
}
