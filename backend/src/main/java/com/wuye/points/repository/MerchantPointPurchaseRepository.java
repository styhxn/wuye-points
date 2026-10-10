package com.wuye.points.repository;

import com.wuye.points.entity.MerchantPointPurchase;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 商家积分购买 Repository
 */
@Repository
public interface MerchantPointPurchaseRepository extends JpaRepository<MerchantPointPurchase, String>,
        JpaSpecificationExecutor<MerchantPointPurchase> {

    Page<MerchantPointPurchase> findByMerchantId(String merchantId, Pageable pageable);

    Page<MerchantPointPurchase> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);

    Page<MerchantPointPurchase> findByAuditStatus(String auditStatus, Pageable pageable);

    Optional<MerchantPointPurchase> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);

    /**
     * 取该商家「已审核通过且仍有余额」的购买记录，按时间升序（FIFO），并加行级写锁。
     * 供积分消费扣减（如积分支付广告位）使用：加锁保证并发扣减时余额校验与扣减原子，避免透支。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from MerchantPointPurchase p "
            + "where p.merchantId = :merchantId and p.auditStatus = 'approved' and p.remainingPoints > 0 "
            + "order by p.createdAt asc")
    List<MerchantPointPurchase> findApprovedWithRemainingForUpdate(@Param("merchantId") String merchantId);
}
