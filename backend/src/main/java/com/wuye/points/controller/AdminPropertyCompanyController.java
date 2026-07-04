package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.propertycompany.CreatePropertyCompanyRequest;
import com.wuye.points.dto.propertycompany.CreatePropertyCompanyResponse;
import com.wuye.points.dto.propertycompany.PropertyCompanyDetailResponse;
import com.wuye.points.dto.propertycompany.PropertyCompanyListItemResponse;
import com.wuye.points.dto.propertycompany.UpdatePropertyCompanyRequest;
import com.wuye.points.service.PropertyCompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

@Tag(name = "管理端 — 物业公司管理", description = "物业公司列表、创建、详情、更新、状态、配置")
@RestController
@RequestMapping("/admin/property-companies")
@RequiredArgsConstructor
public class AdminPropertyCompanyController {

    private final PropertyCompanyService propertyCompanyService;

    @Operation(summary = "物业公司列表", description = "获取物业公司列表。角色: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<PageResponse<PropertyCompanyListItemResponse>> listPropertyCompanies(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "物业公司状态") @RequestParam(required = false) String status,
            @Parameter(description = "排序字段", example = "-createdAt") @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(propertyCompanyService.listPropertyCompanies(
                page, pageSize, keyword, status, sort));
    }

    @Operation(summary = "创建物业公司", description = "创建物业公司。角色: property_admin, platform_admin")
    @PostMapping
    public ApiResponse<CreatePropertyCompanyResponse> createPropertyCompany(
            @Valid @RequestBody CreatePropertyCompanyRequest request) {
        return ApiResponse.success(propertyCompanyService.createPropertyCompany(request));
    }

    @Operation(summary = "物业公司详情", description = "获取物业公司详情。角色: property_admin, platform_admin")
    @GetMapping("/{id}")
    public ApiResponse<PropertyCompanyDetailResponse> getPropertyCompany(
            @Parameter(description = "资源ID") @PathVariable String id) {
        return ApiResponse.success(propertyCompanyService.getPropertyCompany(id));
    }

    @Operation(summary = "更新物业公司", description = "更新物业公司信息。角色: property_admin, platform_admin")
    @PutMapping("/{id}")
    public ApiResponse<PropertyCompanyDetailResponse> updatePropertyCompany(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Valid @RequestBody UpdatePropertyCompanyRequest request) {
        return ApiResponse.success(propertyCompanyService.updatePropertyCompany(id, request));
    }

    @Operation(summary = "更新物业公司状态", description = "更新物业公司状态。角色: property_admin, platform_admin")
    @PatchMapping("/{id}/status")
    public ApiResponse<PropertyCompanyDetailResponse> updateStatus(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Valid @RequestBody UpdatePropertyCompanyRequest request) {
        return ApiResponse.success(propertyCompanyService.updatePropertyCompany(id, request));
    }

    @Operation(summary = "更新物业公司配置", description = "更新物业公司配置信息。角色: property_admin, platform_admin")
    @PatchMapping("/{id}/config")
    public ApiResponse<PropertyCompanyDetailResponse> updateConfig(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Valid @RequestBody UpdatePropertyCompanyRequest request) {
        return ApiResponse.success(propertyCompanyService.updatePropertyCompany(id, request));
    }
}
