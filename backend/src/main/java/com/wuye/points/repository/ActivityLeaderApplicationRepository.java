package com.wuye.points.repository;

import com.wuye.points.entity.ActivityLeaderApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActivityLeaderApplicationRepository
        extends JpaRepository<ActivityLeaderApplication, String>, JpaSpecificationExecutor<ActivityLeaderApplication> {

    Optional<ActivityLeaderApplication> findFirstByResidentIdAndPropertyCompanyIdAndAuditStatusOrderByCreatedAtDesc(
            String residentId, String propertyCompanyId, String auditStatus);

    Optional<ActivityLeaderApplication> findFirstByResidentIdAndPropertyCompanyIdOrderByCreatedAtDesc(
            String residentId, String propertyCompanyId);
}
