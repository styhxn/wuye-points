package com.wuye.points.repository;

import com.wuye.points.entity.PropertyCompany;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 物业公司 Repository
 * 注：物业公司本身无需 propertyCompanyId 过滤
 */
@Repository
public interface PropertyCompanyRepository extends JpaRepository<PropertyCompany, String>,
        JpaSpecificationExecutor<PropertyCompany> {

    Optional<PropertyCompany> findById(String id);

    Page<PropertyCompany> findByNameContaining(String name, Pageable pageable);
}
