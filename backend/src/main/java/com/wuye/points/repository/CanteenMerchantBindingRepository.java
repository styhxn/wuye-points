package com.wuye.points.repository;

import com.wuye.points.entity.CanteenMerchantBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 社区食堂-主商家子商家绑定 Repository
 */
@Repository
public interface CanteenMerchantBindingRepository
        extends JpaRepository<CanteenMerchantBinding, String>,
        JpaSpecificationExecutor<CanteenMerchantBinding> {

    List<CanteenMerchantBinding> findByMainMerchantIdAndStatus(String mainMerchantId, String status);

    /** 子商家被哪些主商家绑定（供消费时选体系） */
    List<CanteenMerchantBinding> findBySubMerchantIdAndStatus(String subMerchantId, String status);

    Optional<CanteenMerchantBinding> findByMainMerchantIdAndSubMerchantId(String mainMerchantId, String subMerchantId);

    Optional<CanteenMerchantBinding> findByMainMerchantIdAndSubMerchantIdAndPropertyCompanyId(
            String mainMerchantId, String subMerchantId, String propertyCompanyId);

    long countByMainMerchantIdAndStatus(String mainMerchantId, String status);
}
