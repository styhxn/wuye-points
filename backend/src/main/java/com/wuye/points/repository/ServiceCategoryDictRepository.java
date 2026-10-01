package com.wuye.points.repository;

import com.wuye.points.entity.ServiceCategoryDict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceCategoryDictRepository extends JpaRepository<ServiceCategoryDict, String> {

    List<ServiceCategoryDict> findByStatusOrderBySortOrderAsc(String status);
}
