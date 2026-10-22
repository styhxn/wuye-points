package com.wuye.points.repository;

import com.wuye.points.entity.ResidentMerchantSettlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface ResidentMerchantSettlementRepository extends JpaRepository<ResidentMerchantSettlement, String> {

    Page<ResidentMerchantSettlement> findByResidentIdOrderByCreatedAtDesc(String residentId, Pageable pageable);

    boolean existsByOrderId(String orderId);

    /** 按订单查询结算记录（退款冲正时定位原结算） */
    Optional<ResidentMerchantSettlement> findFirstByOrderId(String orderId);

    /** 业主卖货累计收益（已结算），用于业主卖货钱包可提现余额计算 */
    @Query("""
            SELECT COALESCE(SUM(s.resellerProfit), 0) FROM ResidentMerchantSettlement s
            WHERE s.residentId = :residentId
              AND s.status = 'settled'
            """)
    BigDecimal sumResellerProfitByResidentId(@Param("residentId") String residentId);

    Page<ResidentMerchantSettlement> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ResidentMerchantSettlement> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start, LocalDateTime end, Pageable pageable);
}
