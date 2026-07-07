package com.wuye.points.repository;

import com.wuye.points.entity.PropertyFee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 物业费 Repository
 */
@Repository
public interface PropertyFeeRepository extends JpaRepository<PropertyFee, String>, JpaSpecificationExecutor<PropertyFee> {

    boolean existsByResidentIdAndPeriod(String residentId, String period);

    @Query("SELECT COUNT(f) FROM PropertyFee f WHERE f.residentId = :residentId AND f.paymentStatus NOT IN ('paid', 'cancelled')")
    long countByResidentIdAndPaymentStatusNotIn(@Param("residentId") String residentId);

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

    @Query("""
            SELECT COALESCE(SUM(f.amount), 0) FROM PropertyFee f
            WHERE f.propertyCompanyId = :propertyCompanyId
            """)
    BigDecimal sumAmountByPropertyCompanyId(@Param("propertyCompanyId") String propertyCompanyId);

    @Query("""
            SELECT COALESCE(SUM(f.paidAmount), 0) FROM PropertyFee f
            WHERE f.propertyCompanyId = :propertyCompanyId AND f.paymentStatus = 'paid'
            """)
    BigDecimal sumPaidAmountByPropertyCompanyId(@Param("propertyCompanyId") String propertyCompanyId);
}
