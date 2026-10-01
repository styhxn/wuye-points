package com.wuye.points.repository;

import com.wuye.points.entity.ResidentMerchantApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ResidentMerchantApplicationRepository extends JpaRepository<ResidentMerchantApplication, String>,
        JpaSpecificationExecutor<ResidentMerchantApplication> {

    Optional<ResidentMerchantApplication> findByResidentId(String residentId);

    /**
     * 公开橱窗详情：对外(public)且营业中(active)的业主商户
     */
    Optional<ResidentMerchantApplication> findByResidentIdAndVisibilityAndStatus(
            String residentId, String visibility, String status);

    Page<ResidentMerchantApplication> findByPropertyCompanyIdOrderByCreatedAtDesc(
            String propertyCompanyId, Pageable pageable);

    Page<ResidentMerchantApplication> findByPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
            String propertyCompanyId, String status, Pageable pageable);

    Page<ResidentMerchantApplication> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
