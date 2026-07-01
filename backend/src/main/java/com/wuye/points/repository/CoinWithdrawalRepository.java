package com.wuye.points.repository;

import com.wuye.points.entity.CoinWithdrawal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 物业币兑换现金 Repository
 */
@Repository
public interface CoinWithdrawalRepository extends JpaRepository<CoinWithdrawal, String>,
        JpaSpecificationExecutor<CoinWithdrawal> {

    Page<CoinWithdrawal> findByMerchantId(String merchantId, Pageable pageable);

    Page<CoinWithdrawal> findByAuditStatus(String auditStatus, Pageable pageable);

    Page<CoinWithdrawal> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);

    Optional<CoinWithdrawal> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);

    boolean existsByMerchantIdAndAuditStatus(String merchantId, String auditStatus);
}
