package com.wuye.points.repository;

import com.wuye.points.entity.MerchantCommunityDistance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 商家-小区距离配置 Repository
 */
@Repository
public interface MerchantCommunityDistanceRepository extends JpaRepository<MerchantCommunityDistance, String> {

    List<MerchantCommunityDistance> findByMerchantId(String merchantId);

    Optional<MerchantCommunityDistance> findByMerchantIdAndCommunityId(String merchantId, String communityId);

    /**
     * 批量取多个商家与指定小区的距离配置（列表页一次 IN 查询，避免 N+1）。
     */
    List<MerchantCommunityDistance> findByCommunityIdAndMerchantIdIn(
            String communityId, Collection<String> merchantIds);
}
