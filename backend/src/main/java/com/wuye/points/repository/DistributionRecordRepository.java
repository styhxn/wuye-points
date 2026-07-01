package com.wuye.points.repository;

import com.wuye.points.entity.DistributionRecord;
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
 * 四级分成记录 Repository
 */
@Repository
public interface DistributionRecordRepository extends JpaRepository<DistributionRecord, String>,
        JpaSpecificationExecutor<DistributionRecord> {

    List<DistributionRecord> findByPropertyCompanyId(String propertyCompanyId);

    List<DistributionRecord> findByOrderId(String orderId);

    Optional<DistributionRecord> findFirstByOrderId(String orderId);

    List<DistributionRecord> findByCoordinatorId(String coordinatorId);

    List<DistributionRecord> findBySectorLeaderId(String sectorLeaderId);

    @Query("""
            SELECT COALESCE(SUM(d.distributableAmount), 0) FROM DistributionRecord d
            WHERE (:propertyCompanyId IS NULL OR d.propertyCompanyId = :propertyCompanyId)
              AND (:startDate IS NULL OR d.createdAt >= :startDate)
              AND (:endDate IS NULL OR d.createdAt < :endDate)
            """)
    BigDecimal sumDistributableAmount(@Param("propertyCompanyId") String propertyCompanyId,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    @Query("""
            SELECT COALESCE(SUM(d.propertyShare), 0) FROM DistributionRecord d
            WHERE (:propertyCompanyId IS NULL OR d.propertyCompanyId = :propertyCompanyId)
              AND (:startDate IS NULL OR d.createdAt >= :startDate)
              AND (:endDate IS NULL OR d.createdAt < :endDate)
            """)
    BigDecimal sumPropertyShare(@Param("propertyCompanyId") String propertyCompanyId,
                                @Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);

    @Query("""
            SELECT COALESCE(SUM(d.coordinatorShare), 0) FROM DistributionRecord d
            WHERE (:propertyCompanyId IS NULL OR d.propertyCompanyId = :propertyCompanyId)
              AND (:startDate IS NULL OR d.createdAt >= :startDate)
              AND (:endDate IS NULL OR d.createdAt < :endDate)
            """)
    BigDecimal sumCoordinatorShare(@Param("propertyCompanyId") String propertyCompanyId,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("""
            SELECT COALESCE(SUM(d.sectorLeaderShare), 0) FROM DistributionRecord d
            WHERE (:propertyCompanyId IS NULL OR d.propertyCompanyId = :propertyCompanyId)
              AND (:startDate IS NULL OR d.createdAt >= :startDate)
              AND (:endDate IS NULL OR d.createdAt < :endDate)
            """)
    BigDecimal sumSectorLeaderShare(@Param("propertyCompanyId") String propertyCompanyId,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    @Query("""
            SELECT COALESCE(SUM(d.individualLeaderShare), 0) FROM DistributionRecord d
            WHERE (:propertyCompanyId IS NULL OR d.propertyCompanyId = :propertyCompanyId)
              AND (:startDate IS NULL OR d.createdAt >= :startDate)
              AND (:endDate IS NULL OR d.createdAt < :endDate)
            """)
    BigDecimal sumIndividualLeaderShare(@Param("propertyCompanyId") String propertyCompanyId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    @Query("""
            SELECT COALESCE(SUM(d.coordinatorShare), 0) FROM DistributionRecord d
            WHERE d.coordinatorId = :coordinatorId
            """)
    BigDecimal sumCoordinatorShareByCoordinatorId(@Param("coordinatorId") String coordinatorId);

    @Query("""
            SELECT COALESCE(SUM(d.sectorLeaderShare), 0) FROM DistributionRecord d
            WHERE d.sectorLeaderId = :sectorLeaderId
            """)
    BigDecimal sumSectorLeaderShareBySectorLeaderId(@Param("sectorLeaderId") String sectorLeaderId);

    @Query("""
            SELECT COALESCE(SUM(d.individualLeaderShare), 0) FROM DistributionRecord d
            WHERE d.individualLeaderId = :individualLeaderId
            """)
    BigDecimal sumIndividualLeaderShareByIndividualLeaderId(@Param("individualLeaderId") String individualLeaderId);
}
