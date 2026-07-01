package com.wuye.points.repository;

import com.wuye.points.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 配送 Repository
 */
@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, String>, JpaSpecificationExecutor<Delivery> {

    Optional<Delivery> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);

    Optional<Delivery> findByOrderId(String orderId);

    Optional<Delivery> findByOrderIdAndPropertyCompanyId(String orderId, String propertyCompanyId);

    @Query("""
            SELECT d FROM Delivery d
            WHERE d.status = 'pending' AND d.createdAt < :cutoff
            """)
    List<Delivery> findPendingBefore(@Param("cutoff") LocalDateTime cutoff);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Delivery d SET
                d.courierId = :courierId,
                d.status = 'accepted',
                d.acceptedAt = :acceptedAt,
                d.courierEarning = :courierEarning
            WHERE d.id = :id AND d.status = 'pending'
            """)
    int grabIfPending(@Param("id") String id,
                      @Param("courierId") String courierId,
                      @Param("acceptedAt") LocalDateTime acceptedAt,
                      @Param("courierEarning") BigDecimal courierEarning);
}
