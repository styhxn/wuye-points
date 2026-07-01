package com.wuye.points.repository;

import com.wuye.points.entity.PointRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 积分记录 Repository
 */
@Repository
public interface PointRecordRepository extends JpaRepository<PointRecord, String>, JpaSpecificationExecutor<PointRecord> {

    Page<PointRecord> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);

    Page<PointRecord> findByResidentId(String residentId, Pageable pageable);

    Page<PointRecord> findByFamilyId(String familyId, Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(r.amount), 0) FROM PointRecord r
            WHERE r.residentId = :residentId AND r.pointType = 'earn'
            AND r.createdAt >= :start AND r.createdAt < :end
            """)
    Integer sumEarnedBetween(@Param("residentId") String residentId,
                             @Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end);

    @Query("""
            SELECT COALESCE(SUM(ABS(r.amount)), 0) FROM PointRecord r
            WHERE r.residentId = :residentId AND r.pointType = 'spend'
            AND r.createdAt >= :start AND r.createdAt < :end
            """)
    Integer sumSpentBetween(@Param("residentId") String residentId,
                            @Param("start") LocalDateTime start,
                            @Param("end") LocalDateTime end);

    @Query("""
            SELECT COALESCE(SUM(r.amount), 0) FROM PointRecord r
            WHERE r.familyId = :familyId AND r.pointType = 'earn'
            AND r.createdAt >= :start AND r.createdAt < :end
            """)
    Integer sumFamilyEarnedBetween(@Param("familyId") String familyId,
                                   @Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

    @Query("""
            SELECT COALESCE(SUM(ABS(r.amount)), 0) FROM PointRecord r
            WHERE r.familyId = :familyId AND r.pointType = 'spend'
            AND r.createdAt >= :start AND r.createdAt < :end
            """)
    Integer sumFamilySpentBetween(@Param("familyId") String familyId,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);
}
