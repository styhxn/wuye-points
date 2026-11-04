package com.wuye.points.service;

import com.wuye.points.dto.merchantcategory.MerchantCategoryItemResponse;
import com.wuye.points.entity.MerchantCategoryDict;
import com.wuye.points.repository.MerchantCategoryDictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * 可用于新建/改类的品类名（仅 active）。
     *
     * <p>品类清单以前有两份：前端下拉读这张字典表，后端校验读 {@code RoleGuard} 里的硬编码 Set，
     * 两边已经漂移（字典多出「休闲玩乐」，选它就 400）。以字典表为唯一事实源。</p>
     */
    @Transactional(readOnly = true)
    public Set<String> activeCategoryNames() {
        return repository.findAllByStatusOrderBySortOrderAsc("active").stream()
                .map(MerchantCategoryDict::getName)
                .collect(Collectors.toUnmodifiableSet());
    }

    /** 字典里出现过的品类名（含 inactive）：查询过滤参数用，历史商家可能仍挂在已下架品类上。 */
    @Transactional(readOnly = true)
    public Set<String> knownCategoryNames() {
        return repository.findAll().stream()
                .map(MerchantCategoryDict::getName)
                .collect(Collectors.toUnmodifiableSet());
    }

    private MerchantCategoryItemResponse toItem(MerchantCategoryDict dict) {
        return MerchantCategoryItemResponse.builder()
                .name(dict.getName())
                .sortOrder(dict.getSortOrder())
                .build();
    }
}
