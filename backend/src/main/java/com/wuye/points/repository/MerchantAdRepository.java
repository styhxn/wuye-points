package com.wuye.points.repository;

import com.wuye.points.entity.MerchantAd;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantAdRepository extends JpaRepository<MerchantAd, String> {

    Page<MerchantAd> findByMerchantIdOrderByCreatedAtDesc(String merchantId, Pageable pageable);
}
