package com.wuye.points.repository;

import com.wuye.points.entity.ActivityGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 活动组 Repository
 */
@Repository
public interface ActivityGroupRepository extends JpaRepository<ActivityGroup, String>, JpaSpecificationExecutor<ActivityGroup> {

    Page<ActivityGroup> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);

    Optional<ActivityGroup> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);
}
