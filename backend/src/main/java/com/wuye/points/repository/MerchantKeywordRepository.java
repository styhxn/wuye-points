package com.wuye.points.repository;

import com.wuye.points.entity.MerchantKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 商家关键词 Repository
 */
@Repository
public interface MerchantKeywordRepository extends JpaRepository<MerchantKeyword, String> {

    List<MerchantKeyword> findByMerchantId(String merchantId);

    List<MerchantKeyword> findByMerchantIdAndStatus(String merchantId, String status);

    List<MerchantKeyword> findByKeywordIn(List<String> keywords);

    List<MerchantKeyword> findByKeywordInAndStatus(List<String> keywords, String status);

    Optional<MerchantKeyword> findByMerchantIdAndKeyword(String merchantId, String keyword);

    boolean existsByMerchantIdAndKeyword(String merchantId, String keyword);

    void deleteByMerchantId(String merchantId);
}
