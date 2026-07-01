package com.wuye.points.repository;

import com.wuye.points.entity.MerchantPointGrant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商家积分录入 Repository
 * 注：purchase_id / resident_id 已隐含 propertyCompanyId
 */
@Repository
public interface MerchantPointGrantRepository extends JpaRepository<MerchantPointGrant, String> {

    List<MerchantPointGrant> findByPurchaseId(String purchaseId);

    List<MerchantPointGrant> findByResidentId(String residentId);
}
