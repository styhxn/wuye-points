package com.wuye.points.repository;

import com.wuye.points.entity.DistributorApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 一级代理申请表 Repository
 */
@Repository
public interface DistributorApplicationRepository extends JpaRepository<DistributorApplication, String>,
        JpaSpecificationExecutor<DistributorApplication> {

    Optional<DistributorApplication> findFirstByResidentIdAndPropertyCompanyIdAndAuditStatusOrderByCreatedAtDesc(
            String residentId, String propertyCompanyId, String auditStatus);

    Optional<DistributorApplication> findFirstByResidentIdAndPropertyCompanyIdOrderByCreatedAtDesc(
            String residentId, String propertyCompanyId);

    long countByPropertyCompanyIdAndAuditStatus(String propertyCompanyId, String auditStatus);
}
