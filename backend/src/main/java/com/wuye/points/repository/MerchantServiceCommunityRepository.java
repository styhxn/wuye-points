package com.wuye.points.repository;

import com.wuye.points.entity.MerchantServiceCommunity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MerchantServiceCommunityRepository extends JpaRepository<MerchantServiceCommunity, String> {

    List<MerchantServiceCommunity> findByMerchantId(String merchantId);

    void deleteByMerchantId(String merchantId);

    boolean existsByMerchantIdAndCommunityId(String merchantId, String communityId);
}
