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
import java.util.List;
import java.util.Optional;

/**
 * 订单 Repository
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {

    Page<Order> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);

    Page<Order> findByResidentId(String residentId, Pageable pageable);

    Page<Order> findByMerchantId(String merchantId, Pageable pageable);

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
}
