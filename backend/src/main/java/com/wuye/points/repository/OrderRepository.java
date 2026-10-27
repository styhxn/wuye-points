package com.wuye.points.repository;

import com.wuye.points.entity.Order;
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
 * 订单 Repository
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {

    @Query("SELECT COUNT(o) FROM Order o WHERE o.residentId = :residentId AND o.orderStatus NOT IN ('completed', 'cancelled')")
    long countPendingOrdersByResidentId(@Param("residentId") String residentId);

    Page<Order> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);

    Page<Order> findByResidentId(String residentId, Pageable pageable);

    Page<Order> findByMerchantId(String merchantId, Pageable pageable);

    Page<Order> findByMerchantIdAndOrderStatus(String merchantId, String orderStatus, Pageable pageable);

    Optional<Order> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o
            WHERE o.residentId = :residentId AND o.orderStatus = 'completed'
            """)
    BigDecimal sumCompletedOrderAmount(@Param("residentId") String residentId);

    @Query("""
            SELECT COUNT(o) FROM Order o
            WHERE o.residentId = :residentId AND o.orderStatus = 'completed'
            """)
    long countCompletedOrders(@Param("residentId") String residentId);

    @Query("""
            SELECT DISTINCT o.merchantId FROM Order o
            WHERE o.residentId = :residentId AND o.orderStatus = 'completed'
            AND o.merchantId IS NOT NULL
            """)
    List<String> findDistinctMerchantIdsByResidentId(@Param("residentId") String residentId);

    @Query("""
            SELECT DISTINCT oi.productId FROM OrderItem oi
            JOIN Order o ON oi.orderId = o.id
            WHERE o.residentId = :residentId AND o.orderStatus = 'completed'
            AND oi.productId IS NOT NULL
            """)
    List<String> findDistinctProductIdsByResidentId(@Param("residentId") String residentId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.propertyCompanyId = :propertyCompanyId")
    long countByPropertyCompanyId(@Param("propertyCompanyId") String propertyCompanyId);

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o
            WHERE o.propertyCompanyId = :propertyCompanyId AND o.orderStatus = 'completed'
            """)
    BigDecimal sumRevenueByPropertyCompanyId(@Param("propertyCompanyId") String propertyCompanyId);

    @Query("""
            SELECT o.merchantId, COUNT(o) AS cnt, COALESCE(SUM(o.totalAmount), 0) AS rev
            FROM Order o
            WHERE o.propertyCompanyId = :propertyCompanyId AND o.orderStatus = 'completed'
            AND o.merchantId IS NOT NULL
            GROUP BY o.merchantId
            ORDER BY cnt DESC
            """)
    List<Object[]> findTopMerchantsByPropertyCompanyId(@Param("propertyCompanyId") String propertyCompanyId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.merchantId = :merchantId AND o.orderStatus = 'completed'")
    BigDecimal sumCompletedRevenueByMerchantId(@Param("merchantId") String merchantId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.merchantId = :merchantId AND o.orderStatus = 'completed'")
    long countCompletedByMerchantId(@Param("merchantId") String merchantId);

    /** 商家累计订单总数（不含已取消/已退款，供商家工作台汇总） */
    @Query("""
            SELECT COUNT(o) FROM Order o
            WHERE o.merchantId = :merchantId
              AND o.orderStatus NOT IN ('cancelled', 'refunded')
            """)
    long countAllByMerchantId(@Param("merchantId") String merchantId);

    /** 一级代理商用户购买记录（代理货源订单） */
    Page<Order> findByDistributorId(String distributorId, Pageable pageable);

    Page<Order> findByDistributorIdAndOrderStatus(String distributorId, String orderStatus, Pageable pageable);

    List<Order> findByOrderStatusAndVerificationDeadlineBefore(String orderStatus, LocalDateTime deadline);

    List<Order> findByOrderStatusAndFreezeEndDateBefore(String orderStatus, LocalDateTime deadline);

    List<Order> findByFulfillmentModeAndFulfillmentDeadlineBefore(String fulfillmentMode, LocalDateTime deadline);

    /** 超期自动确认收货：已送达且超过售后窗口期仍处于 delivering 的订单 */
    List<Order> findByOrderStatusAndDeliveredAtBefore(String orderStatus, LocalDateTime cutoff);

    /** 业主小店上架是否存在未完成订单（取消上架前校验）：未完成 = 非 completed/cancelled/refunded */
    @Query("""
            SELECT COUNT(o) FROM Order o
            WHERE o.listingId = :listingId
              AND o.orderStatus NOT IN ('completed', 'cancelled', 'refunded')
            """)
    long countUnfinishedByListingId(@Param("listingId") String listingId);
}
