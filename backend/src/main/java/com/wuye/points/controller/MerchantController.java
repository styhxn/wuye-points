package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.merchant.CreateMerchantRequest;
import com.wuye.points.dto.merchant.MerchantDetailResponse;
import com.wuye.points.dto.merchant.MerchantListItemResponse;
import com.wuye.points.dto.merchant.UpdateMerchantRequest;
import com.wuye.points.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家模块
 */
@Tag(name = "商家模块", description = "商家列表、入驻申请、详情、更新")
@RestController
@RequestMapping("/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    /**
     * 商家列表（分页/筛选）
     * Auth: Optional | Roles: any
     */
    @Operation(summary = "商家列表", description = "商家列表（分页/筛选），无需鉴权")
    @GetMapping
    public ApiResponse<PageResponse<MerchantListItemResponse>> listMerchants(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "商家分类") @RequestParam(required = false) String category,
            @Parameter(description = "商家等级") @RequestParam(required = false) String merchantLevel,
            @Parameter(description = "审核状态") @RequestParam(required = false) String auditStatus,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "排序字段", example = "-rankOrder") @RequestParam(defaultValue = "-rankOrder") String sort) {
        return ApiResponse.success(merchantService.listMerchants(
                page, pageSize, keyword, category, merchantLevel, auditStatus, propertyCompanyId, sort));
    }

    /**
     * 我的商家
     * Auth: Required | Roles: merchant
     */
    @Operation(summary = "我的商家", description = "获取我的商家信息，需登录，角色：merchant")
    @GetMapping("/my")
    public ApiResponse<MerchantDetailResponse> getMyMerchant() {
        return ApiResponse.success(merchantService.getMyMerchant());
    }

    /**
     * 商家详情
     * Auth: Optional | Roles: any
     */
    @Operation(summary = "商家详情", description = "获取商家详情，无需鉴权")
    @GetMapping("/{id}")
    public ApiResponse<MerchantDetailResponse> getMerchant(
            @Parameter(description = "商家ID", example = "1") @PathVariable String id) {
        return ApiResponse.success(merchantService.getMerchant(id));
    }

    /**
     * 商家入驻申请
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "商家入驻申请", description = "商家入驻申请，需登录，角色：resident")
    @PostMapping
    public ApiResponse<MerchantDetailResponse> applyMerchant(
            @Valid @RequestBody CreateMerchantRequest request) {
        return ApiResponse.success(merchantService.applyMerchant(request));
    }

    /**
     * 更新商家信息
     * Auth: Required | Roles: merchant, property_admin, platform_admin
     */
    @Operation(summary = "更新商家信息", description = "更新商家信息，需登录，角色：merchant, property_admin, platform_admin")
    @PutMapping("/{id}")
    public ApiResponse<MerchantDetailResponse> updateMerchant(
            @Parameter(description = "商家ID", example = "1") @PathVariable String id,
            @Valid @RequestBody UpdateMerchantRequest request) {
        return ApiResponse.success(merchantService.updateMerchant(id, request));
    }
}
