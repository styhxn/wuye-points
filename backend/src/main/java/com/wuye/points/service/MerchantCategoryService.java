package com.wuye.points.service;

import com.wuye.points.dto.merchantcategory.MerchantCategoryItemResponse;
import com.wuye.points.entity.MerchantCategoryDict;
import com.wuye.points.repository.MerchantCategoryDictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantCategoryService {

    private final MerchantCategoryDictRepository repository;

    @Transactional(readOnly = true)
    public List<MerchantCategoryItemResponse> listActiveCategories() {
        return repository.findAllByStatusOrderBySortOrderAsc("active").stream()
                .map(this::toItem)
                .toList();
    }

    private MerchantCategoryItemResponse toItem(MerchantCategoryDict dict) {
        return MerchantCategoryItemResponse.builder()
                .name(dict.getName())
                .sortOrder(dict.getSortOrder())
                .build();
    }
}
