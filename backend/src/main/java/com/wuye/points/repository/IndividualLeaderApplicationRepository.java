package com.wuye.points.repository;

import com.wuye.points.entity.IndividualLeaderApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IndividualLeaderApplicationRepository extends JpaRepository<IndividualLeaderApplication, String>,
        JpaSpecificationExecutor<IndividualLeaderApplication> {

    Optional<IndividualLeaderApplication> findFirstByResidentIdAndPropertyCompanyIdAndAuditStatusOrderByCreatedAtDesc(
            String residentId, String propertyCompanyId, String auditStatus);

    long countByPropertyCompanyIdAndAuditStatus(String propertyCompanyId, String auditStatus);
}
