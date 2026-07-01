package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.product.CreateProductRequest;
import com.wuye.points.dto.product.DeleteProductResponse;
import com.wuye.points.dto.product.ProductDetailResponse;
import com.wuye.points.dto.product.ProductListItemResponse;
import com.wuye.points.dto.product.UpdateProductRequest;
import com.wuye.points.service.ProductService;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品模块
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 商品列表（分页/筛选）
     * Auth: Optional | Roles: any
     */
    @GetMapping
    public ApiResponse<PageResponse<ProductListItemResponse>> listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String propertyCompanyId,
            @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(productService.listProducts(
                page, pageSize, keyword, merchantId, category, status, propertyCompanyId, sort));
    }

    /**
     * 商品详情
     * Auth: Optional | Roles: any
     */
    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> getProduct(@PathVariable String id) {
        return ApiResponse.success(productService.getProduct(id));
    }

    /**
     * 创建商品
     * Auth: Required | Roles: merchant
     */
    @PostMapping
    public ApiResponse<ProductDetailResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        return ApiResponse.success(productService.createProduct(request));
    }

    /**
     * 更新商品
     * Auth: Required | Roles: merchant
     */
    @PutMapping("/{id}")
    public ApiResponse<ProductDetailResponse> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ApiResponse.success(productService.updateProduct(id, request));
    }

    /**
     * 删除商品（软删除，status=inactive）
     * Auth: Required | Roles: merchant
     */
    @DeleteMapping("/{id}")
    public ApiResponse<DeleteProductResponse> deleteProduct(@PathVariable String id) {
        return ApiResponse.success(productService.deleteProduct(id));
    }
}
