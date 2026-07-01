package com.wuye.points.repository;

import com.wuye.points.entity.DeliveryRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryRuleRepository extends JpaRepository<DeliveryRule, String> {
    Optional<DeliveryRule> findByPropertyCompanyId(String propertyCompanyId);

    Page<DeliveryRule> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);
}
