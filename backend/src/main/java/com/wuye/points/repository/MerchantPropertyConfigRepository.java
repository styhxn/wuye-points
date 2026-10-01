package com.wuye.points.repository;

import com.wuye.points.entity.MerchantPropertyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 商家-物业参数配置 Repository
 */
@Repository
public interface MerchantPropertyConfigRepository extends JpaRepository<MerchantPropertyConfig, String> {

    Optional<MerchantPropertyConfig> findByMerchantIdAndPropertyCompanyId(String merchantId, String propertyCompanyId);

    Optional<MerchantPropertyConfig> findByMerchantId(String merchantId);
}
