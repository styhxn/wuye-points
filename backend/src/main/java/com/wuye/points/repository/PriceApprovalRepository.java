package com.wuye.points.repository;

import com.wuye.points.entity.PriceApproval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 价格审批记录 Repository
 */
@Repository
public interface PriceApprovalRepository extends JpaRepository<PriceApproval, String>,
        JpaSpecificationExecutor<PriceApproval> {

    List<PriceApproval> findByPropertyCompanyIdOrderByCreatedAtDesc(String propertyCompanyId);

    List<PriceApproval> findByPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
            String propertyCompanyId, String status);

    List<PriceApproval> findByApplicantIdOrderByCreatedAtDesc(String applicantId);

    Page<PriceApproval> findByPropertyCompanyIdOrderByCreatedAtDesc(String propertyCompanyId, Pageable pageable);

    Page<PriceApproval> findByPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
            String propertyCompanyId, String status, Pageable pageable);

    Page<PriceApproval> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
}
