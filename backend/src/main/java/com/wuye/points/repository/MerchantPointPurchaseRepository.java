package com.wuye.points.repository;

import com.wuye.points.entity.MerchantPointPurchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

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
}
