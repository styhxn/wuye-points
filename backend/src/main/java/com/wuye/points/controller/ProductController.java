package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.product.CreateProductRequest;
import com.wuye.points.dto.product.DeleteProductResponse;
import com.wuye.points.dto.product.ProductDetailResponse;
import com.wuye.points.dto.product.ProductListItemResponse;
import com.wuye.points.dto.product.UpdateProductRecommendRequest;
import com.wuye.points.dto.product.UpdateProductRequest;
import com.wuye.points.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品模块
 */
@Tag(name = "商品模块", description = "商品列表、创建、更新、删除")
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "商品列表", description = "商品列表（分页/筛选）。Auth: Optional | Roles: any")
    @GetMapping
    public ApiResponse<PageResponse<ProductListItemResponse>> listProducts(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "商家ID") @RequestParam(required = false) String merchantId,
            @Parameter(description = "商品分类") @RequestParam(required = false) String category,
            @Parameter(description = "商品状态") @RequestParam(required = false) String status,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "商品类型") @RequestParam(required = false) String productType,
            @Parameter(description = "仅看推荐商品") @RequestParam(required = false) Boolean recommended,
            @Parameter(description = "排序字段（-降序，+升序）", example = "-createdAt") @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(productService.listProducts(
                page, pageSize, keyword, merchantId, category, status, propertyCompanyId, productType, recommended, sort));
    }

    @Operation(summary = "商品详情", description = "商品详情。Auth: Optional | Roles: any")
    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> getProduct(
            @Parameter(description = "商品ID") @PathVariable String id) {
        return ApiResponse.success(productService.getProduct(id));
    }

    @Operation(summary = "创建商品", description = "创建商品。Auth: Required | Roles: merchant")
    @PostMapping
    public ApiResponse<ProductDetailResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        return ApiResponse.success(productService.createProduct(request));
    }

    @Operation(summary = "更新商品", description = "更新商品。Auth: Required | Roles: merchant")
    @PutMapping("/{id}")
    public ApiResponse<ProductDetailResponse> updateProduct(
            @Parameter(description = "商品ID") @PathVariable String id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ApiResponse.success(productService.updateProduct(id, request));
    }

    @Operation(summary = "删除商品", description = "删除商品（软删除，status=inactive）。Auth: Required | Roles: merchant")
    @DeleteMapping("/{id}")
    public ApiResponse<DeleteProductResponse> deleteProduct(
            @Parameter(description = "商品ID") @PathVariable String id) {
        return ApiResponse.success(productService.deleteProduct(id));
    }

    @Operation(summary = "推荐/取消推荐商品", description = "后台设置推荐商品。Roles: property_admin, platform_admin")
    @PutMapping("/{id}/recommend")
    public ApiResponse<Void> setRecommended(
            @Parameter(description = "商品ID") @PathVariable String id,
            @Valid @RequestBody UpdateProductRecommendRequest request) {
        productService.setRecommended(id, Boolean.TRUE.equals(request.getRecommended()), request.getRecommendedSort());
        return ApiResponse.success(null);
    }
}
