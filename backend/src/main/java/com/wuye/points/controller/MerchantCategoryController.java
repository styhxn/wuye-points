package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.merchantcategory.MerchantCategoryItemResponse;
import com.wuye.points.service.MerchantCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 商家分类字典（前端）
 * 供入驻下拉、商家列表筛选使用，匿名可访问
 */
@Tag(name = "商家分类", description = "商家分类字典，入驻与列表筛选使用")
@RestController
@RequestMapping("/merchant-categories")
@RequiredArgsConstructor
public class MerchantCategoryController {

    private final MerchantCategoryService merchantCategoryService;

    @Operation(summary = "商家分类列表", description = "获取全部已启用的商家分类，无需鉴权。前端入驻/筛选场景均从此接口取值。")
    @GetMapping
    public ApiResponse<Map<String, List<MerchantCategoryItemResponse>>> listCategories() {
        return ApiResponse.success(Map.of("list", merchantCategoryService.listActiveCategories()));
    }
}
