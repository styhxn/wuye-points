package com.wuye.points.repository;

import com.wuye.points.entity.CanteenQuotaPurchase;
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
 * 社区食堂-额度购买流水 Repository
 */
@Repository
public interface CanteenQuotaPurchaseRepository
        extends JpaRepository<CanteenQuotaPurchase, String>,
        JpaSpecificationExecutor<CanteenQuotaPurchase> {

    Optional<CanteenQuotaPurchase> findByPaymentRecordId(String paymentRecordId);

    List<CanteenQuotaPurchase> findByMerchantIdAndStatus(String merchantId, String status);

    Page<CanteenQuotaPurchase> findByMerchantId(String merchantId, Pageable pageable);

    /** 汇总主商家已到账额度的剩余可充值额度 */
    @Query("SELECT COALESCE(SUM(p.remainingQuota), 0) FROM CanteenQuotaPurchase p "
            + "WHERE p.merchantId = :merchantId AND p.status = 'paid'")
    BigDecimal sumRemainingQuota(@Param("merchantId") String merchantId);

    /** 已到账、仍有剩余额度的购买记录，按到账时间先后排序（充值扣减顺序） */
    @Query("SELECT p FROM CanteenQuotaPurchase p WHERE p.merchantId = :merchantId "
            + "AND p.status = 'paid' AND p.remainingQuota > 0 "
            + "ORDER BY p.completedAt ASC, p.createdAt ASC")
    List<CanteenQuotaPurchase> findSpendableByMerchantId(@Param("merchantId") String merchantId);
}
