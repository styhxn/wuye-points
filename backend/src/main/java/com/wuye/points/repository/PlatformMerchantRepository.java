package com.wuye.points.repository;

import com.wuye.points.entity.PlatformMerchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 平台级商家库 Repository
 * 注：跨物业共享，无需 propertyCompanyId 过滤
 */
@Repository
public interface PlatformMerchantRepository extends JpaRepository<PlatformMerchant, String>,
        JpaSpecificationExecutor<PlatformMerchant> {

    List<PlatformMerchant> findByCategory(String category);

    Page<PlatformMerchant> findByNameContaining(String name, Pageable pageable);
}
