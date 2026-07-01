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

/**
 * 物业币记录 Repository
 */
@Repository
public interface PropertyCoinRecordRepository extends JpaRepository<PropertyCoinRecord, String>,
        JpaSpecificationExecutor<PropertyCoinRecord> {

    Page<PropertyCoinRecord> findByFamilyId(String familyId, Pageable pageable);

    Page<PropertyCoinRecord> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);

    Page<PropertyCoinRecord> findByResidentId(String residentId, Pageable pageable);

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
}
