package com.wuye.points.repository;

import com.wuye.points.entity.MerchantCategoryDict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantCategoryDictRepository extends JpaRepository<MerchantCategoryDict, String> {

    List<MerchantCategoryDict> findAllByStatusOrderBySortOrderAsc(String status);

    boolean existsByNameAndStatus(String name, String status);
}
