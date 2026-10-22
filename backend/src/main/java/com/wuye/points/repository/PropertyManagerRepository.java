package com.wuye.points.repository;

import com.wuye.points.entity.PropertyManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PropertyManagerRepository extends JpaRepository<PropertyManager, String>, JpaSpecificationExecutor<PropertyManager> {

    Optional<PropertyManager> findByResidentIdAndStatus(String residentId, String status);

    Optional<PropertyManager> findByPropertyCompanyIdAndStatus(String propertyCompanyId, String status);

    List<PropertyManager> findByPropertyCompanyIdOrderByCreatedAtDesc(String propertyCompanyId);

    List<PropertyManager> findByResidentId(String residentId);

    Optional<PropertyManager> findFirstByPropertyCompanyIdAndStatusOrderByIdAsc(String propertyCompanyId, String status);

    /** 解析当前登录住户在其物业公司的在职经理身份（同物业仅一名 active） */
    Optional<PropertyManager> findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByIdAsc(
            String residentId, String propertyCompanyId, String status);
}
