package com.wuye.points.repository;

import com.wuye.points.entity.PointRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 积分规则 Repository
 */
@Repository
public interface PointRuleRepository extends JpaRepository<PointRule, String> {

    List<PointRule> findByPropertyCompanyId(String propertyCompanyId);

    List<PointRule> findBySource(String source);
}
