package com.wuye.points.repository;

import com.wuye.points.entity.PropertyOperatorScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyOperatorScopeRepository extends JpaRepository<PropertyOperatorScope, String>,
        JpaSpecificationExecutor<PropertyOperatorScope> {

    Optional<PropertyOperatorScope> findByResidentIdAndPropertyCompanyId(
            String residentId, String propertyCompanyId);

    List<PropertyOperatorScope> findByPropertyCompanyId(String propertyCompanyId);
}
