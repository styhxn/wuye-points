package com.wuye.points.repository;

import com.wuye.points.entity.ProfileCompletionReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 信息采集积分奖励配置 Repository
 */
@Repository
public interface ProfileCompletionRewardRepository extends JpaRepository<ProfileCompletionReward, String> {

    List<ProfileCompletionReward> findByScopeAndScopeId(String scope, String scopeId);

    Optional<ProfileCompletionReward> findByScopeAndScopeIdAndFieldName(String scope, String scopeId, String fieldName);

    List<ProfileCompletionReward> findByFieldName(String fieldName);
}
