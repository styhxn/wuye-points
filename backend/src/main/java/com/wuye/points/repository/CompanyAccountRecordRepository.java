package com.wuye.points.repository;

import com.wuye.points.entity.CompanyAccountRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 公司账户资金流水 Repository
 */
@Repository
public interface CompanyAccountRecordRepository extends JpaRepository<CompanyAccountRecord, String> {

    Page<CompanyAccountRecord> findByPropertyCompanyIdOrderByCreatedAtDesc(
            String propertyCompanyId, Pageable pageable);

    Optional<CompanyAccountRecord> findFirstByPropertyCompanyIdOrderByCreatedAtDesc(
            String propertyCompanyId);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM CompanyAccountRecord r WHERE r.propertyCompanyId = :propertyCompanyId")
    BigDecimal sumAmountByPropertyCompanyId(@Param("propertyCompanyId") String propertyCompanyId);

    /**
     * 幂等校验：同一 orderId + source 是否已处理过
     */
    boolean existsByOrderIdAndSource(String orderId, String source);
}
