package com.wuye.points.repository;

import com.wuye.points.entity.DeliveryPriceRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 物业公司配送价格区间 Repository
 */
@Repository
public interface DeliveryPriceRangeRepository extends JpaRepository<DeliveryPriceRange, String>,
        JpaSpecificationExecutor<DeliveryPriceRange> {

    List<DeliveryPriceRange> findByPropertyCompanyIdAndStatus(String propertyCompanyId, String status);

    List<DeliveryPriceRange> findByPropertyCompanyIdOrderByMinPriceAsc(String propertyCompanyId);

    /**
     * 查询同物业公司、同 distanceType + deliveryScope 维度下的 active 区间（用于重叠校验）。
     */
    List<DeliveryPriceRange> findByPropertyCompanyIdAndStatusAndDistanceTypeAndDeliveryScope(
            String propertyCompanyId, String status, String distanceType, String deliveryScope);
}
