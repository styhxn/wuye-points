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

    List<DistributionRecord> findByOrderIdIn(java.util.Collection<String> orderIds);

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

    org.springframework.data.domain.Page<DistributionRecord> findByBuildingLeaderIdOrderByCreatedAtDesc(
            String buildingLeaderId, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<DistributionRecord> findByMerchantIdOrderByCreatedAtDesc(
            String merchantId, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<DistributionRecord> findByCoordinatorIdOrderByCreatedAtDesc(
            String coordinatorId, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<DistributionRecord> findBySectorLeaderIdOrderByCreatedAtDesc(
            String sectorLeaderId, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<DistributionRecord> findByIndividualLeaderIdOrderByCreatedAtDesc(
            String individualLeaderId, org.springframework.data.domain.Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(d.buildingLeaderShare), 0) FROM DistributionRecord d
            WHERE d.buildingLeaderId = :buildingLeaderId
            """)
    BigDecimal sumBuildingLeaderShareByBuildingLeaderId(@Param("buildingLeaderId") String buildingLeaderId);

    @Query("""
            SELECT d.buildingLeaderId, COALESCE(SUM(d.buildingLeaderShare), 0)
            FROM DistributionRecord d
            WHERE d.buildingLeaderId IN :buildingLeaderIds
            GROUP BY d.buildingLeaderId
            """)
    List<Object[]> sumBuildingLeaderShareGrouped(@Param("buildingLeaderIds") java.util.Collection<String> buildingLeaderIds);

    @Query("""
            SELECT COALESCE(SUM(d.buildingLeaderShare), 0) FROM DistributionRecord d
            WHERE (:propertyCompanyId IS NULL OR d.propertyCompanyId = :propertyCompanyId)
              AND (:startDate IS NULL OR d.createdAt >= :startDate)
              AND (:endDate IS NULL OR d.createdAt < :endDate)
            """)
    BigDecimal sumBuildingLeaderShare(@Param("propertyCompanyId") String propertyCompanyId,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * 商家已完成订单应得 = Σ(商品金额 − 抽佣 − 商家承担的配送补贴)。
     * 商品金额 = 订单总额 − 优惠券（优惠券由商家承担，不进入抽佣基数）。
     * 平台/物业/负责人份额全部从抽佣中分配，不能再从商家应得里二次扣减。
     */
    @Query("SELECT COALESCE(SUM(" +
           "CASE WHEN ((o.totalAmount - COALESCE(o.couponAmount, 0)) " +
           "  - ((o.totalAmount - COALESCE(o.couponAmount, 0)) * d.commissionRate) " +
           "  - CASE WHEN o.deliverySubsidySponsor = 'merchant' " +
           "         THEN COALESCE(o.deliverySubsidyAmount, 0) ELSE 0 END) < 0 THEN 0 " +
           "ELSE ((o.totalAmount - COALESCE(o.couponAmount, 0)) " +
           "  - ((o.totalAmount - COALESCE(o.couponAmount, 0)) * d.commissionRate) " +
           "  - CASE WHEN o.deliverySubsidySponsor = 'merchant' " +
           "         THEN COALESCE(o.deliverySubsidyAmount, 0) ELSE 0 END) END), 0) " +
           "FROM DistributionRecord d, Order o " +
           "WHERE d.orderId = o.id AND d.merchantId = :merchantId AND o.orderStatus = 'completed'")
    BigDecimal sumMerchantIncomeForCompletedOrders(@Param("merchantId") String merchantId);

    @Query("SELECT COALESCE(SUM(" +
           "CASE WHEN ((o.totalAmount - COALESCE(o.couponAmount, 0)) " +
           "  - ((o.totalAmount - COALESCE(o.couponAmount, 0)) * d.commissionRate) " +
           "  - CASE WHEN o.deliverySubsidySponsor = 'merchant' " +
           "         THEN COALESCE(o.deliverySubsidyAmount, 0) ELSE 0 END) < 0 " +
           "THEN -((o.totalAmount - COALESCE(o.couponAmount, 0)) " +
           "  - ((o.totalAmount - COALESCE(o.couponAmount, 0)) * d.commissionRate) " +
           "  - CASE WHEN o.deliverySubsidySponsor = 'merchant' " +
           "         THEN COALESCE(o.deliverySubsidyAmount, 0) ELSE 0 END) " +
           "ELSE 0 END), 0) " +
           "FROM DistributionRecord d, Order o " +
           "WHERE d.orderId = o.id AND d.merchantId = :merchantId AND o.orderStatus = 'completed'")
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
