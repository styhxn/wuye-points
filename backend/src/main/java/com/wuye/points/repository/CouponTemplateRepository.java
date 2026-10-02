package com.wuye.points.repository;

import com.wuye.points.entity.CouponTemplate;
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
 * 优惠券模板 Repository
 */
@Repository
public interface CouponTemplateRepository
        extends JpaRepository<CouponTemplate, String>, JpaSpecificationExecutor<CouponTemplate> {

    /** 领取/发放时行锁，保证发放总量与已领数量原子递增 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM CouponTemplate t WHERE t.id = :id")
    Optional<CouponTemplate> findByIdForUpdate(@Param("id") String id);

    /**
     * 领券中心：可领取的券（启用中、未领完）。
     * 平台券全量可见；商家券仅同物业可见。
     */
    @Query("SELECT t FROM CouponTemplate t WHERE t.status = 'active' "
            + "AND (t.totalQuantity IS NULL OR t.claimedQuantity < t.totalQuantity) "
            + "AND (t.scope = 'platform' "
            + "     OR (t.scope = 'merchant' AND t.propertyCompanyId = :propertyCompanyId)) "
            + "AND (:merchantId IS NULL OR t.merchantId = :merchantId) "
            + "ORDER BY t.createdAt DESC")
    List<CouponTemplate> findClaimable(@Param("propertyCompanyId") String propertyCompanyId,
                                       @Param("merchantId") String merchantId);

    /** 管理端列表：平台券全量可见；商家券按物业归属过滤 */
    @Query("SELECT t FROM CouponTemplate t WHERE "
            + "(:propertyCompanyId IS NULL OR t.scope = 'platform' OR t.propertyCompanyId = :propertyCompanyId) "
            + "AND (:scope IS NULL OR t.scope = :scope) "
            + "AND (:status IS NULL OR t.status = :status) "
            + "AND (:merchantId IS NULL OR t.merchantId = :merchantId) "
            + "ORDER BY t.createdAt DESC")
    Page<CouponTemplate> findAllForAdmin(@Param("propertyCompanyId") String propertyCompanyId,
                                         @Param("scope") String scope,
                                         @Param("status") String status,
                                         @Param("merchantId") String merchantId,
                                         Pageable pageable);
}