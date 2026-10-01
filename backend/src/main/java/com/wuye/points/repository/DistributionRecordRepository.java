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

    List<DistributionRecord> findByIndividualLeaderId(String individualLeaderId);

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

    @Query("SELECT COALESCE(SUM(d.totalAmount - " +
           "(CASE WHEN d.propertyShare IS NULL THEN 0 ELSE d.propertyShare END) - " +
           "(CASE WHEN d.coordinatorShare IS NULL THEN 0 ELSE d.coordinatorShare END) - " +
           "(CASE WHEN d.sectorLeaderShare IS NULL THEN 0 ELSE d.sectorLeaderShare END) - " +
           "(CASE WHEN d.individualLeaderShare IS NULL THEN 0 ELSE d.individualLeaderShare END)), 0) " +
           "FROM DistributionRecord d WHERE d.merchantId = :merchantId")
    BigDecimal sumMerchantIncomeForCompletedOrders(@Param("merchantId") String merchantId);

    @Query("SELECT COALESCE(SUM(d.totalAmount), 0) FROM DistributionRecord d WHERE d.merchantId = :merchantId")
    BigDecimal sumMerchantReceivableForCompletedOrders(@Param("merchantId") String merchantId);

    @Query("SELECT COALESCE(SUM(" +
           "(CASE WHEN d.propertyShare IS NULL THEN 0 ELSE d.propertyShare END) + " +
           "(CASE WHEN d.coordinatorShare IS NULL THEN 0 ELSE d.coordinatorShare END) + " +
           "(CASE WHEN d.sectorLeaderShare IS NULL THEN 0 ELSE d.sectorLeaderShare END) + " +
           "(CASE WHEN d.individualLeaderShare IS NULL THEN 0 ELSE d.individualLeaderShare END)), 0) " +
           "FROM DistributionRecord d WHERE d.merchantId = :merchantId")
    BigDecimal sumCommissionByMerchantIdForCompletedOrders(@Param("merchantId") String merchantId);

    @Query("SELECT COALESCE(SUM(d.platformShare), 0) FROM DistributionRecord d " +
           "WHERE d.propertyCompanyId = :propertyCompanyId AND d.createdAt >= :start AND d.createdAt < :end")
    BigDecimal sumPlatformShare(@Param("propertyCompanyId") String propertyCompanyId,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(d.platformEarning), 0) FROM DistributionRecord d " +
           "WHERE d.propertyCompanyId = :propertyCompanyId AND d.createdAt >= :start AND d.createdAt < :end")
    BigDecimal sumPlatformEarning(@Param("propertyCompanyId") String propertyCompanyId,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);
}
