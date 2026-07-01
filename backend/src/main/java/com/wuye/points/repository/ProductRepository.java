package com.wuye.points.repository;

import com.wuye.points.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品 Repository
 * 注：merchant_id 已隐含 propertyCompanyId（同一商家挂接唯一物业）
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {

    List<Product> findByMerchantId(String merchantId);

    List<Product> findTop20ByMerchantIdAndStatusOrderByCreatedAtDesc(String merchantId, String status);
}
