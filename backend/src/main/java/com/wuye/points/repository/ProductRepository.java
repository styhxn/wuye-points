package com.wuye.points.repository;

import com.wuye.points.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品 Repository
 * 注：merchant_id 已隐含 propertyCompanyId（同一商家挂接唯一物业）
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {

    @Query("SELECT p FROM Product p WHERE p.merchantId = :merchantId AND p.deletedAt IS NULL")
    List<Product> findByMerchantId(@Param("merchantId") String merchantId);

    @Query("SELECT p FROM Product p WHERE p.merchantId = :merchantId AND p.status = :status AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Product> findTop20ByMerchantIdAndStatusOrderByCreatedAtDesc(@Param("merchantId") String merchantId, @Param("status") String status);
}
