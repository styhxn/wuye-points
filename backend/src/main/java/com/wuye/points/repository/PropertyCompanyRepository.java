package com.wuye.points.repository;

import com.wuye.points.entity.PropertyCompany;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    boolean existsByName(String name);

    Page<PropertyCompany> findByNameContaining(String name, Pageable pageable);

    @Modifying
    @Query("UPDATE PropertyCompany p SET p.companyAccountBalance = COALESCE(p.companyAccountBalance, 0) + :amount WHERE p.id = :id")
    void addCompanyAccountBalance(@Param("id") String id, @Param("amount") BigDecimal amount);

    List<PropertyCompany> findByAutoWithdrawalEnabledTrue();
}
