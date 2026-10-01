package com.wuye.points.repository;

import com.wuye.points.entity.MerchantWechatAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 商户微信二级商户账号 Repository（电商收付通）。
 */
public interface MerchantWechatAccountRepository extends JpaRepository<MerchantWechatAccount, String> {

    Optional<MerchantWechatAccount> findByMerchantId(String merchantId);

    Optional<MerchantWechatAccount> findByMerchantIdAndStatus(String merchantId, String status);
}
