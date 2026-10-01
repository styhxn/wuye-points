package com.wuye.points.service;

import com.wuye.points.dto.productcategory.ProductCategoryItemResponse;
import com.wuye.points.entity.ProductCategoryDict;
import com.wuye.points.repository.ProductCategoryDictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryDictRepository repository;

    @Transactional(readOnly = true)
    public List<ProductCategoryItemResponse> listActiveCategories() {
        return repository.findAllByStatusOrderBySortOrderAsc("active").stream()
                .map(this::toItem)
                .toList();
    }

    private ProductCategoryItemResponse toItem(ProductCategoryDict dict) {
        return ProductCategoryItemResponse.builder()
                .name(dict.getName())
                .sortOrder(dict.getSortOrder())
                .build();
    }
}
