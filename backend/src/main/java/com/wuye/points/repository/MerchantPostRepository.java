package com.wuye.points.repository;

import com.wuye.points.entity.MerchantPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantPostRepository extends JpaRepository<MerchantPost, String> {

    Page<MerchantPost> findByMerchantIdOrderByCreatedAtDesc(String merchantId, Pageable pageable);

    Page<MerchantPost> findByMerchantIdAndStatusOrderByCreatedAtDesc(String merchantId, String status, Pageable pageable);
}
