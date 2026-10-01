package com.wuye.points.repository;

import com.wuye.points.entity.MerchantMessageServiceConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantMessageServiceConfigRepository extends JpaRepository<MerchantMessageServiceConfig, String> {

    Optional<MerchantMessageServiceConfig> findByMerchantId(String merchantId);
}
