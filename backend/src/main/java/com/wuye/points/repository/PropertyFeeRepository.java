package com.wuye.points.repository;

import com.wuye.points.entity.PropertyFee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 物业费 Repository
 */
@Repository
public interface PropertyFeeRepository extends JpaRepository<PropertyFee, String>, JpaSpecificationExecutor<PropertyFee> {

    Page<PropertyFee> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);

    Page<PropertyFee> findByResidentId(String residentId, Pageable pageable);

    Optional<PropertyFee> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);
}
