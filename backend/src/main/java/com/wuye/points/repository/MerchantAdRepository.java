package com.wuye.points.repository;

import com.wuye.points.entity.MerchantAd;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MerchantAdRepository extends JpaRepository<MerchantAd, String> {

    Page<MerchantAd> findByMerchantIdOrderByCreatedAtDesc(String merchantId, Pageable pageable);

    /** 补付时串行化同一广告的并发请求，避免重复生成支付流水。 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from MerchantAd a where a.id = :id")
    Optional<MerchantAd> findByIdForUpdate(@Param("id") String id);
}
