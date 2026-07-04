package com.wuye.points.repository;

import com.wuye.points.entity.Merchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 商家 Repository
 */
@Repository
public interface MerchantRepository extends JpaRepository<Merchant, String>, JpaSpecificationExecutor<Merchant> {

    Page<Merchant> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);

    Optional<Merchant> findByPlatformMerchantIdAndPropertyCompanyId(String platformMerchantId, String propertyCompanyId);

    Optional<Merchant> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);

    boolean existsByPlatformMerchantIdAndPropertyCompanyId(String platformMerchantId, String propertyCompanyId);

    List<Merchant> findByContactPhoneAndPropertyCompanyId(String contactPhone, String propertyCompanyId);

    long countByPropertyCompanyId(String propertyCompanyId);

    long countByPropertyCompanyIdAndAuditStatus(String propertyCompanyId, String auditStatus);

    long countByPlatformMerchantId(String platformMerchantId);

    Optional<Merchant> findFirstByContactPhoneAndPropertyCompanyIdOrderByCreatedAtDesc(
            String contactPhone, String propertyCompanyId);
}
