package com.wuye.points.repository;

import com.wuye.points.entity.WithdrawalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 多角色通用提现记录 Repository
 */
@Repository
public interface WithdrawalRecordRepository extends JpaRepository<WithdrawalRecord, String>,
        JpaSpecificationExecutor<WithdrawalRecord> {

    @Query("""
            SELECT COALESCE(SUM(w.platformFeeShare), 0) FROM WithdrawalRecord w
            WHERE (:propertyCompanyId IS NULL OR w.propertyCompanyId = :propertyCompanyId)
              AND (:startDate IS NULL OR w.createdAt >= :startDate)
              AND (:endDate IS NULL OR w.createdAt < :endDate)
            """)
    BigDecimal sumPlatformFeeShare(@Param("propertyCompanyId") String propertyCompanyId,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * 按申请人聚合提现金额（用于快递负责人/个体负责人/配送员可提现余额计算）
     *
     * @param applicantId    申请人主体ID（cm_/res_/il_）
     * @param withdrawalType  提现类型 individual_leader/courier
     * @param auditStatus     提现状态过滤；null 表示占用余额的状态（pending+approved+completed，不含 rejected）
     */
    @Query("""
            SELECT COALESCE(SUM(w.amount), 0) FROM WithdrawalRecord w
            WHERE w.applicantId = :applicantId
              AND w.withdrawalType = :withdrawalType
              AND (
                    (:auditStatus IS NOT NULL AND w.auditStatus = :auditStatus)
                 OR (:auditStatus IS NULL AND w.auditStatus IN ('pending', 'approved', 'completed'))
              )
            """)
    BigDecimal sumAmountByApplicant(@Param("applicantId") String applicantId,
                                    @Param("withdrawalType") String withdrawalType,
                                    @Param("auditStatus") String auditStatus);

    // ============== 管理端审批看板汇总 ==============

    @Query("""
            SELECT COUNT(w) FROM WithdrawalRecord w
            WHERE (:propertyCompanyId IS NULL OR w.propertyCompanyId = :propertyCompanyId)
              AND w.auditStatus = :auditStatus
            """)
    long countByPropertyCompanyIdAndAuditStatus(@Param("propertyCompanyId") String propertyCompanyId,
                                                @Param("auditStatus") String auditStatus);

    @Query("""
            SELECT COALESCE(SUM(w.amount), 0) FROM WithdrawalRecord w
            WHERE (:propertyCompanyId IS NULL OR w.propertyCompanyId = :propertyCompanyId)
              AND w.auditStatus = :auditStatus
            """)
    BigDecimal sumAmountByPropertyCompanyIdAndAuditStatus(@Param("propertyCompanyId") String propertyCompanyId,
                                                          @Param("auditStatus") String auditStatus);

    @Query("""
            SELECT COUNT(w), COALESCE(SUM(w.actualAmount), 0) FROM WithdrawalRecord w
            WHERE (:propertyCompanyId IS NULL OR w.propertyCompanyId = :propertyCompanyId)
              AND w.auditStatus IN ('approved', 'completed')
            """)
    List<Object[]> countAndSumApproved(@Param("propertyCompanyId") String propertyCompanyId);

    @Query("""
            SELECT COUNT(w), COALESCE(SUM(w.actualAmount), 0) FROM WithdrawalRecord w
            WHERE (:propertyCompanyId IS NULL OR w.propertyCompanyId = :propertyCompanyId)
              AND w.auditedAt >= :start
              AND w.auditedAt < :end
            """)
    List<Object[]> countAndSumAuditedBetween(@Param("propertyCompanyId") String propertyCompanyId,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);
}
