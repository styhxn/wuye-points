package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.productcategory.ProductCategoryItemResponse;
import com.wuye.points.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 业主商城商品类目字典（前端）
 * 业主商城预置筛选项统一从此接口下发，匿名可访问
 */
@Tag(name = "业主商城商品类目", description = "业主商城商品类目字典，筛选使用")
@RestController
@RequestMapping("/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @Operation(summary = "商品类目列表", description = "获取全部已启用的业主商城商品类目，无需鉴权。")
    @GetMapping
    public ApiResponse<Map<String, List<ProductCategoryItemResponse>>> listCategories() {
        return ApiResponse.success(Map.of("list", productCategoryService.listActiveCategories()));
    }
}
