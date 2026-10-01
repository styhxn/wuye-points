package com.wuye.points.repository;

import com.wuye.points.entity.DistributorProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DistributorProductRepository extends JpaRepository<DistributorProduct, String>,
        JpaSpecificationExecutor<DistributorProduct> {

    Page<DistributorProduct> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    Page<DistributorProduct> findByDistributorMerchantIdOrderByCreatedAtDesc(String distributorMerchantId, Pageable pageable);

    Page<DistributorProduct> findByDistributorMerchantIdAndStatusNotOrderByCreatedAtDesc(
            String distributorMerchantId, String status, Pageable pageable);

    List<DistributorProduct> findByDistributorMerchantId(String distributorMerchantId);
}
