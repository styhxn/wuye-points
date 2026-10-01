package com.wuye.points.repository;

import com.wuye.points.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 询价记录 Repository
 */
@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, String>,
        JpaSpecificationExecutor<Inquiry> {

    List<Inquiry> findByInquirerIdOrderByCreatedAtDesc(String inquirerId);

    List<Inquiry> findByMerchantIdOrderByCreatedAtDesc(String merchantId);

    List<Inquiry> findByMerchantIdAndStatusOrderByCreatedAtDesc(String merchantId, String status);

    List<Inquiry> findByPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
            String propertyCompanyId, String status);

    List<Inquiry> findByPropertyCompanyIdOrderByCreatedAtDesc(String propertyCompanyId);

    Page<Inquiry> findByInquirerIdOrderByCreatedAtDesc(String inquirerId, Pageable pageable);

    Page<Inquiry> findByMerchantIdOrderByCreatedAtDesc(String merchantId, Pageable pageable);
}
