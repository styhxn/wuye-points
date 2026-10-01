package com.wuye.points.repository;

import com.wuye.points.entity.ResidentMerchantListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ResidentMerchantListingRepository extends JpaRepository<ResidentMerchantListing, String>,
        JpaSpecificationExecutor<ResidentMerchantListing> {

    long countByResidentIdAndEnabledTrue(String residentId);

    List<ResidentMerchantListing> findByResidentIdOrderByCreatedAtDesc(String residentId);

    /**
     * 公开橱窗：仅返回启用中的上架商品
     */
    List<ResidentMerchantListing> findByResidentIdAndEnabledTrueOrderByCreatedAtDesc(String residentId);

    /**
     * 公开橱窗列表：批量统计各店铺启用商品数
     */
    List<ResidentMerchantListing> findByResidentIdInAndEnabledTrue(List<String> residentIds);

    List<ResidentMerchantListing> findByDistributorProductIdInOrderByCreatedAtDesc(List<String> distributorProductIds);
}
