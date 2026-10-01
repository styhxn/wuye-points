package com.wuye.points.controller;

import com.wuye.points.dto.admin.residentmerchant.CreateDistributorProductRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.distributor.AdjustDistributorProductStockRequest;
import com.wuye.points.dto.distributor.DistributorMyProductItemResponse;
import com.wuye.points.dto.distributor.DistributorPickupItemResponse;
import com.wuye.points.dto.distributor.DistributorProductItemResponse;
import com.wuye.points.dto.distributor.UpdateDistributorProductRequest;
import com.wuye.points.service.DistributorWorkbenchService;
import com.wuye.points.service.ResidentShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "一级货源", description = "业主商户选品池 + 一级经销商工作台")
@RestController
@RequestMapping("/distributors")
@RequiredArgsConstructor
public class DistributorController {

    private final ResidentShopService residentShopService;
    private final DistributorWorkbenchService distributorWorkbenchService;

    @Operation(summary = "一级货源选品池", description = "需登录，角色：resident")
    @GetMapping("/products")
    public ApiResponse<PageResponse<DistributorProductItemResponse>> listProducts(
            @Parameter(description = "分类ID") @RequestParam(required = false) String categoryId,
            @Parameter(description = "分类ID（兼容 category 参数名）") @RequestParam(required = false) String category,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize) {
        String resolvedCategoryId = categoryId != null ? categoryId : category;
        return ApiResponse.success(residentShopService.listDistributorProducts(resolvedCategoryId, keyword, page, pageSize));
    }

    // ============== 一级经销商手机端工作台 ==============

    @Operation(summary = "经销商上架商品",
            description = "一级经销商自助上架货源商品，归属当前登录商家。角色: merchant（official_certified）")
    @PostMapping("/my/products")
    public ApiResponse<DistributorMyProductItemResponse> createMyProduct(
            @Valid @RequestBody CreateDistributorProductRequest request) {
        return ApiResponse.success(distributorWorkbenchService.createMyProduct(request));
    }

    @Operation(summary = "经销商我的商品列表",
            description = "分页返回本经销商商品及下游拿货数。角色: merchant（official_certified）")
    @GetMapping("/my/products")
    public ApiResponse<PageResponse<DistributorMyProductItemResponse>> listMyProducts(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(distributorWorkbenchService.listMyProducts(page, pageSize));
    }

    @Operation(summary = "经销商修改商品",
            description = "仅可修改本人商品，未传字段不更新。角色: merchant（official_certified）")
    @PutMapping("/my/products/{id}")
    public ApiResponse<DistributorMyProductItemResponse> updateMyProduct(
            @Parameter(description = "商品ID") @PathVariable String id,
            @Valid @RequestBody UpdateDistributorProductRequest request) {
        return ApiResponse.success(distributorWorkbenchService.updateMyProduct(id, request));
    }

    @Operation(summary = "经销商调整库存",
            description = "delta 正数补货、负数扣减，库存不可为负。角色: merchant（official_certified）")
    @PatchMapping("/my/products/{id}/stock")
    public ApiResponse<DistributorMyProductItemResponse> adjustMyProductStock(
            @Parameter(description = "商品ID") @PathVariable String id,
            @Valid @RequestBody AdjustDistributorProductStockRequest request) {
        return ApiResponse.success(distributorWorkbenchService.adjustMyProductStock(id, request));
    }

    @Operation(summary = "经销商删除商品",
            description = "软删除：商品置为 deleted 状态，从经销商列表与选品池移除，下游拿货记录保留。角色: merchant（official_certified）")
    @DeleteMapping("/my/products/{id}")
    public ApiResponse<Void> deleteMyProduct(
            @Parameter(description = "商品ID") @PathVariable String id) {
        distributorWorkbenchService.deleteMyProduct(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "经销商拿货记录查询",
            description = "下游业主商户对本经销商商品的拿货/上架记录（含拿货时间）。角色: merchant（official_certified）")
    @GetMapping("/my/pickups")
    public ApiResponse<List<DistributorPickupItemResponse>> listMyPickups() {
        return ApiResponse.success(distributorWorkbenchService.listMyPickups());
    }

    @Operation(summary = "经销商取消业主商户拿货",
            description = "取消后名额释放（产品数量+1），其他业主商户可继续拿货。角色: merchant（official_certified）")
    @PostMapping("/my/pickups/{listingId}/cancel")
    public ApiResponse<Void> cancelMyPickup(
            @Parameter(description = "拿货上架记录ID") @PathVariable String listingId) {
        distributorWorkbenchService.cancelMyPickup(listingId);
        return ApiResponse.success(null);
    }
}
