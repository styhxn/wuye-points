package com.wuye.points.repository;

import com.wuye.points.entity.PropertyCoinRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 物业币记录 Repository
 */
@Repository
public interface PropertyCoinRecordRepository extends JpaRepository<PropertyCoinRecord, String>,
        JpaSpecificationExecutor<PropertyCoinRecord> {

    Page<PropertyCoinRecord> findByFamilyId(String familyId, Pageable pageable);

    Page<PropertyCoinRecord> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);

    Page<PropertyCoinRecord> findByResidentId(String residentId, Pageable pageable);

    /** 是否存在指定来源的物业币流水（manual_issue 使用条件判定：仅物业配发的物业币可用） */
    boolean existsByResidentIdAndSourceAndCoinType(String residentId, String source, String coinType);

    /** 支付回调幂等：同一支付流水是否已生成充值到账记录 */
    boolean existsByResidentIdAndSourceIdAndCoinType(String residentId, String sourceId, String coinType);

    /** 充值状态查询：按支付流水ID查最近一条充值到账记录 */
    Optional<PropertyCoinRecord> findFirstByResidentIdAndSourceIdAndCoinTypeOrderByCreatedAtDesc(
            String residentId, String sourceId, String coinType);

    /** 订单退款/冲正时，查该订单最近一条指定类型的物业币流水（用于还原家庭池/个人池扣减比例） */
    Optional<PropertyCoinRecord> findFirstBySourceIdAndSourceAndCoinTypeOrderByCreatedAtDesc(
            String sourceId, String source, String coinType);

    /** 个人池过期：只取尚未完全过期、且记录未归属家庭的 earn/recharge 流水，按时间升序。 */
    @Query("""
            SELECT r FROM PropertyCoinRecord r
            WHERE r.residentId = :residentId
              AND r.familyId IS NULL
              AND r.coinType IN ('earn', 'recharge')
              AND r.createdAt < :before
              AND (r.expiredAmount IS NULL OR r.expiredAmount < r.amount)
            ORDER BY r.createdAt ASC
            """)
    List<PropertyCoinRecord> findExpirablePersonal(@Param("residentId") String residentId,
                                                   @Param("before") LocalDateTime before);

    /** 家庭池过期：家庭流水 + 成员入家庭前的个人流水（家庭成员余额已并入家庭池）。 */
    @Query("""
            SELECT r FROM PropertyCoinRecord r
            WHERE (r.familyId = :familyId
                   OR (r.familyId IS NULL AND r.residentId IN :residentIds))
              AND r.coinType IN ('earn', 'recharge')
              AND r.createdAt < :before
              AND (r.expiredAmount IS NULL OR r.expiredAmount < r.amount)
            ORDER BY r.createdAt ASC
            """)
    List<PropertyCoinRecord> findExpirableFamily(@Param("familyId") String familyId,
                                                 @Param("residentIds") List<String> residentIds,
                                                 @Param("before") LocalDateTime before);

    @Query("""
            SELECT COALESCE(SUM(r.amount), 0) FROM PropertyCoinRecord r
            WHERE r.residentId = :residentId AND r.coinType = 'earn'
            AND r.createdAt >= :start AND r.createdAt < :end
            """)
    BigDecimal sumEarnedBetween(@Param("residentId") String residentId,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    @Query("""
            SELECT COALESCE(SUM(ABS(r.amount)), 0) FROM PropertyCoinRecord r
            WHERE r.residentId = :residentId AND r.coinType = 'spend'
            AND r.createdAt >= :start AND r.createdAt < :end
            """)
    BigDecimal sumSpentBetween(@Param("residentId") String residentId,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    @Query("""
            SELECT COALESCE(SUM(r.amount), 0) FROM PropertyCoinRecord r
            WHERE r.propertyCompanyId = :propertyCompanyId
            AND r.coinType IN ('earn', 'recharge')
            """)
    BigDecimal sumIssuedByPropertyCompanyId(@Param("propertyCompanyId") String propertyCompanyId);

    @Query("""
            SELECT COALESCE(SUM(ABS(r.amount)), 0) FROM PropertyCoinRecord r
            WHERE r.propertyCompanyId = :propertyCompanyId
            AND r.coinType = 'spend'
            """)
    BigDecimal sumConsumedByPropertyCompanyId(@Param("propertyCompanyId") String propertyCompanyId);

    List<PropertyCoinRecord> findByResidentIdAndCoinTypeAndCreatedAtBefore(
            String residentId, String coinType, LocalDateTime createdAt);

    @Query("""
            SELECT COALESCE(SUM(r.amount), 0) FROM PropertyCoinRecord r
            WHERE r.residentId = :residentId
            AND r.coinType IN ('earn', 'recharge')
            AND r.createdAt > :startWindow
            AND r.createdAt <= :endWindow
            """)
    BigDecimal sumCoinsExpiringBetween(@Param("residentId") String residentId,
                                       @Param("startWindow") LocalDateTime startWindow,
                                       @Param("endWindow") LocalDateTime endWindow);
}
