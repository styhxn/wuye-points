package com.wuye.points.repository;

import com.wuye.points.entity.ActivityGroupPricingTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 活动组多课时价格 Repository
 */
@Repository
public interface ActivityGroupPricingTierRepository extends JpaRepository<ActivityGroupPricingTier, String> {

    List<ActivityGroupPricingTier> findByActivityGroupIdOrderBySortOrder(String activityGroupId);

    void deleteByActivityGroupId(String activityGroupId);
}
