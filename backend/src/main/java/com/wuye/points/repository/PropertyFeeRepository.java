package com.wuye.points.repository;

import com.wuye.points.entity.PropertyFee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 物业费 Repository
 */
@Repository
public interface PropertyFeeRepository extends JpaRepository<PropertyFee, String>, JpaSpecificationExecutor<PropertyFee> {

    Page<PropertyFee> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);

    Page<PropertyFee> findByResidentId(String residentId, Pageable pageable);

    Optional<PropertyFee> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);

    long countByPropertyCompanyIdAndPaymentStatus(String propertyCompanyId, String paymentStatus);

    long countByPropertyCompanyId(String propertyCompanyId);

    @Query("""
            SELECT f FROM PropertyFee f
            WHERE f.residentId = :residentId
            AND f.paymentStatus IN ('unpaid', 'overdue')
            ORDER BY f.dueDate ASC
            """)
    Page<PropertyFee> findOldestUnpaidFees(@Param("residentId") String residentId, Pageable pageable);
}
