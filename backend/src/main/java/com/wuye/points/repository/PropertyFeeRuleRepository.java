package com.wuye.points.repository;

import com.wuye.points.entity.PropertyFeeRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertyFeeRuleRepository extends JpaRepository<PropertyFeeRule, String> {
    Page<PropertyFeeRule> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);
    Optional<PropertyFeeRule> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);
    Optional<PropertyFeeRule> findFirstByPropertyCompanyIdAndFeeTypeAndStatus(
            String propertyCompanyId, String feeType, String status);
}
