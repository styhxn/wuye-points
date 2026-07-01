package com.wuye.points.repository;

import com.wuye.points.entity.PropertyCoinRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertyCoinRuleRepository extends JpaRepository<PropertyCoinRule, String> {
    Page<PropertyCoinRule> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);
    Optional<PropertyCoinRule> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);
}
