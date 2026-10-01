package com.wuye.points.repository;

import com.wuye.points.entity.ServiceOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, String>,
        JpaSpecificationExecutor<ServiceOrder> {
    Page<ServiceOrder> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);
    Page<ServiceOrder> findByResidentId(String residentId, Pageable pageable);
    Optional<ServiceOrder> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);

    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM ServiceOrder s WHERE s.providerId = :providerId AND s.orderStatus = 'completed'")
    BigDecimal sumCompletedAmountByProviderId(@Param("providerId") String providerId);

    @Query("SELECT COUNT(s) FROM ServiceOrder s WHERE s.providerId = :providerId AND s.orderStatus = 'completed'")
    long countCompletedByProviderId(@Param("providerId") String providerId);
}
