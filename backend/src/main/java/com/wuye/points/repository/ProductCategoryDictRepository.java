package com.wuye.points.repository;

import com.wuye.points.entity.ProductCategoryDict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryDictRepository extends JpaRepository<ProductCategoryDict, String> {

    List<ProductCategoryDict> findAllByStatusOrderBySortOrderAsc(String status);

    boolean existsByNameAndStatus(String name, String status);
}
