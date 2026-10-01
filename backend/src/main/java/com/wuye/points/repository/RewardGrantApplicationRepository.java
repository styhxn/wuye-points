package com.wuye.points.repository;

import com.wuye.points.entity.RewardGrantApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardGrantApplicationRepository extends JpaRepository<RewardGrantApplication, String>,
        JpaSpecificationExecutor<RewardGrantApplication> {

    List<RewardGrantApplication> findByApplicantIdOrderByCreatedAtDesc(String applicantId);

    Page<RewardGrantApplication> findByApplicantId(String applicantId, Pageable pageable);

    Page<RewardGrantApplication> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);

    Page<RewardGrantApplication> findByPropertyCompanyIdAndAuditStatus(
            String propertyCompanyId, String status, Pageable pageable);

    long countByPropertyCompanyIdAndAuditStatus(String propertyCompanyId, String status);
}
