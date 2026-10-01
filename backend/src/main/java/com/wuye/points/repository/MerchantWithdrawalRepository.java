package com.wuye.points.repository;

import com.wuye.points.entity.MerchantWithdrawal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Query("SELECT COALESCE(SUM(m.amount), 0) FROM MerchantWithdrawal m " +
           "WHERE m.merchantId = :merchantId AND (:auditStatus IS NULL OR m.auditStatus = :auditStatus)")
    BigDecimal sumAmountByMerchantId(@Param("merchantId") String merchantId, @Param("auditStatus") String auditStatus);

    @Query("SELECT COALESCE(SUM(m.amount), 0) FROM MerchantWithdrawal m WHERE m.merchantId = :merchantId")
    BigDecimal sumAmountByMerchantId(@Param("merchantId") String merchantId);

    @Query("SELECT COALESCE(SUM(m.platformFeeShare), 0) FROM MerchantWithdrawal m " +
           "WHERE m.propertyCompanyId = :propertyCompanyId AND m.completedAt >= :start AND m.completedAt < :end")
    BigDecimal sumPlatformFeeShare(@Param("propertyCompanyId") String propertyCompanyId,
                                    @Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);
}
