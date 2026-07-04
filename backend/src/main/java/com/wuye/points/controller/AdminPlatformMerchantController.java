package com.wuye.points.controller;

import com.wuye.points.dto.admin.merchant.ProfitSpaceResponse;
import com.wuye.points.dto.admin.platformmerchant.BatchConfigRequest;
import com.wuye.points.dto.admin.platformmerchant.BatchConfigResponse;
import com.wuye.points.dto.admin.platformmerchant.CreatePlatformMerchantRequest;
import com.wuye.points.dto.admin.platformmerchant.LinkMerchantRequest;
import com.wuye.points.dto.admin.platformmerchant.LinkMerchantResponse;
import com.wuye.points.dto.admin.platformmerchant.PlatformMerchantDetailResponse;
import com.wuye.points.dto.admin.platformmerchant.PlatformMerchantListItemResponse;
import com.wuye.points.dto.admin.platformmerchant.UpdatePlatformMerchantRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminPlatformMerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Tag(name = "管理端 — 平台商家管理", description = "平台商家列表、创建、详情、更新、盈利空间、批量配置、挂接")
@RestController
@RequestMapping("/admin/platform-merchants")
@RequiredArgsConstructor
public class AdminPlatformMerchantController {

    private final AdminPlatformMerchantService adminPlatformMerchantService;

    @Operation(summary = "平台商家列表", description = "平台商家列表、创建、详情、更新、盈利空间、批量配置、挂接。角色: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<PageResponse<PlatformMerchantListItemResponse>> list(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "商家分类") @RequestParam(required = false) String category,
            @Parameter(description = "商家状态") @RequestParam(required = false) String status,
            @Parameter(description = "关联物业公司ID") @RequestParam(required = false) String linkedPropertyCompanyId) {
        return ApiResponse.success(adminPlatformMerchantService.list(
                page, pageSize, keyword, category, status, linkedPropertyCompanyId));
    }

    @Operation(summary = "创建平台商家", description = "创建平台商家。角色: property_admin, platform_admin")
    @PostMapping
    public ApiResponse<PlatformMerchantDetailResponse> create(
            @Valid @RequestBody CreatePlatformMerchantRequest request) {
        return ApiResponse.success(adminPlatformMerchantService.create(request));
    }

    @Operation(summary = "平台商家详情", description = "获取平台商家详情。角色: property_admin, platform_admin")
    @GetMapping("/{id}")
    public ApiResponse<PlatformMerchantDetailResponse> getById(
            @Parameter(description = "资源ID") @PathVariable String id) {
        return ApiResponse.success(adminPlatformMerchantService.getById(id));
    }

    @Operation(summary = "更新平台商家", description = "更新平台商家信息。角色: property_admin, platform_admin")
    @PutMapping("/{id}")
    public ApiResponse<PlatformMerchantDetailResponse> update(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Valid @RequestBody UpdatePlatformMerchantRequest request) {
        return ApiResponse.success(adminPlatformMerchantService.update(id, request));
    }

    @Operation(summary = "盈利空间", description = "获取平台商家盈利空间信息。角色: property_admin, platform_admin")
    @GetMapping("/{id}/profit-space")
    public ApiResponse<ProfitSpaceResponse> getProfitSpace(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Parameter(description = "消费金额", example = "100") @RequestParam(defaultValue = "100") BigDecimal consumptionAmount) {
        return ApiResponse.success(adminPlatformMerchantService.getProfitSpace(id, consumptionAmount));
    }

    @Operation(summary = "批量配置", description = "批量配置平台商家参数。角色: property_admin, platform_admin")
    @PostMapping("/batch-config")
    public ApiResponse<BatchConfigResponse> batchSetParams(
            @Valid @RequestBody BatchConfigRequest request) {
        return ApiResponse.success(adminPlatformMerchantService.batchSetParams(request));
    }

    @Operation(summary = "挂接到物业公司", description = "将平台商家挂接到物业公司。角色: property_admin, platform_admin")
    @PostMapping("/{id}/link")
    public ApiResponse<LinkMerchantResponse> linkToProperty(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Valid @RequestBody LinkMerchantRequest request) {
        return ApiResponse.success(adminPlatformMerchantService.linkToProperty(id, request));
    }
}
