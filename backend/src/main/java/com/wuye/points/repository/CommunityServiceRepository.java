package com.wuye.points.repository;

import com.wuye.points.entity.CommunityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 社区服务 Repository
 */
@Repository
public interface CommunityServiceRepository extends JpaRepository<CommunityService, String>,
        JpaSpecificationExecutor<CommunityService> {

    Page<CommunityService> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);

    Optional<CommunityService> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);
}
