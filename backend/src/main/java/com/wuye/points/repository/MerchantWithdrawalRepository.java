package com.wuye.points.repository;

import com.wuye.points.entity.MerchantWithdrawal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 商家提现 Repository
 */
@Repository
public interface MerchantWithdrawalRepository extends JpaRepository<MerchantWithdrawal, String>,
        JpaSpecificationExecutor<MerchantWithdrawal> {

    Page<MerchantWithdrawal> findByMerchantId(String merchantId, Pageable pageable);

    Page<MerchantWithdrawal> findByAuditStatus(String auditStatus, Pageable pageable);

    Page<MerchantWithdrawal> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);

    Optional<MerchantWithdrawal> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);
}
