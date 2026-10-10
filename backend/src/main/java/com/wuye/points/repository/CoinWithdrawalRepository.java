package com.wuye.points.repository;

import com.wuye.points.entity.CoinWithdrawal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
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

    /** 按审核状态统计笔数（propertyCompanyId 为空表示不限定物业公司） */
    @Query("SELECT COUNT(c) FROM CoinWithdrawal c " +
            "WHERE (:propertyCompanyId IS NULL OR c.propertyCompanyId = :propertyCompanyId) " +
            "AND c.auditStatus IN :statuses")
    long countByStatuses(@Param("propertyCompanyId") String propertyCompanyId,
                         @Param("statuses") Collection<String> statuses);

    /** 按审核状态汇总实到金额 */
    @Query("SELECT COALESCE(SUM(c.actualAmount), 0) FROM CoinWithdrawal c " +
            "WHERE (:propertyCompanyId IS NULL OR c.propertyCompanyId = :propertyCompanyId) " +
            "AND c.auditStatus IN :statuses")
    BigDecimal sumActualAmountByStatuses(@Param("propertyCompanyId") String propertyCompanyId,
                                         @Param("statuses") Collection<String> statuses);

    /**
     * 统计指定时间区间内完成的笔数。
     * 注意：coin_withdrawals 无 completed_at 字段，审核状态置为 completed 是终态且之后不再变更，
     * 因此以 updated_at 作为完成时间。
     */
    @Query("SELECT COUNT(c) FROM CoinWithdrawal c " +
            "WHERE (:propertyCompanyId IS NULL OR c.propertyCompanyId = :propertyCompanyId) " +
            "AND c.auditStatus = 'completed' AND c.updatedAt >= :start AND c.updatedAt < :end")
    long countCompletedBetween(@Param("propertyCompanyId") String propertyCompanyId,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(c.actualAmount), 0) FROM CoinWithdrawal c " +
            "WHERE (:propertyCompanyId IS NULL OR c.propertyCompanyId = :propertyCompanyId) " +
            "AND c.auditStatus = 'completed' AND c.updatedAt >= :start AND c.updatedAt < :end")
    BigDecimal sumCompletedBetween(@Param("propertyCompanyId") String propertyCompanyId,
                                   @Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);
}
