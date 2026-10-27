package com.wuye.points.repository;

import com.wuye.points.entity.MerchantWithdrawal;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
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

    /** 审核时行锁：防止同一笔提现被并发审核/重复打款 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MerchantWithdrawal m WHERE m.id = :id")
    Optional<MerchantWithdrawal> findByIdForUpdate(@Param("id") String id);

    /** 占用中的提现金额（pending/approved/completed；rejected 不占用可提现余额） */
    @Query("SELECT COALESCE(SUM(m.amount), 0) FROM MerchantWithdrawal m " +
           "WHERE m.merchantId = :merchantId AND m.auditStatus IN :statuses")
    BigDecimal sumAmountByMerchantIdAndStatuses(@Param("merchantId") String merchantId,
                                                @Param("statuses") Collection<String> statuses);

    /** 审核单笔提现时，计算除当前单以外的占用额，避免把当前单重复扣减导致永远判为超额。 */
    @Query("SELECT COALESCE(SUM(m.amount), 0) FROM MerchantWithdrawal m " +
           "WHERE m.merchantId = :merchantId AND m.auditStatus IN :statuses AND m.id <> :excludeId")
    BigDecimal sumAmountByMerchantIdAndStatusesExcluding(@Param("merchantId") String merchantId,
                                                         @Param("statuses") Collection<String> statuses,
                                                         @Param("excludeId") String excludeId);

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

    /** 按审核状态统计笔数（propertyCompanyId 为空表示不限定物业公司） */
    @Query("SELECT COUNT(m) FROM MerchantWithdrawal m " +
           "WHERE (:propertyCompanyId IS NULL OR m.propertyCompanyId = :propertyCompanyId) " +
           "AND m.auditStatus IN :statuses")
    long countByStatuses(@Param("propertyCompanyId") String propertyCompanyId,
                         @Param("statuses") Collection<String> statuses);

    /** 按审核状态汇总申请金额 */
    @Query("SELECT COALESCE(SUM(m.amount), 0) FROM MerchantWithdrawal m " +
           "WHERE (:propertyCompanyId IS NULL OR m.propertyCompanyId = :propertyCompanyId) " +
           "AND m.auditStatus IN :statuses")
    BigDecimal sumAmountByStatuses(@Param("propertyCompanyId") String propertyCompanyId,
                                   @Param("statuses") Collection<String> statuses);

    /** 按审核状态汇总实到金额 */
    @Query("SELECT COALESCE(SUM(m.actualAmount), 0) FROM MerchantWithdrawal m " +
           "WHERE (:propertyCompanyId IS NULL OR m.propertyCompanyId = :propertyCompanyId) " +
           "AND m.auditStatus IN :statuses")
    BigDecimal sumActualAmountByStatuses(@Param("propertyCompanyId") String propertyCompanyId,
                                         @Param("statuses") Collection<String> statuses);

    /** 统计指定时间区间内已审笔数（按 auditedAt） */
    @Query("SELECT COUNT(m) FROM MerchantWithdrawal m " +
           "WHERE (:propertyCompanyId IS NULL OR m.propertyCompanyId = :propertyCompanyId) " +
           "AND m.auditedAt >= :start AND m.auditedAt < :end")
    long countAuditedBetween(@Param("propertyCompanyId") String propertyCompanyId,
                             @Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end);

    /** 汇总指定时间区间内已审实到金额（按 auditedAt） */
    @Query("SELECT COALESCE(SUM(m.actualAmount), 0) FROM MerchantWithdrawal m " +
           "WHERE (:propertyCompanyId IS NULL OR m.propertyCompanyId = :propertyCompanyId) " +
           "AND m.auditedAt >= :start AND m.auditedAt < :end")
    BigDecimal sumActualAmountAuditedBetween(@Param("propertyCompanyId") String propertyCompanyId,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);
}
