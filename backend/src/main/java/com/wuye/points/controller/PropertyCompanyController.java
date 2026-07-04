package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.propertycompany.CommunityListResponse;
import com.wuye.points.dto.propertycompany.CreatePropertyCompanyRequest;
import com.wuye.points.dto.propertycompany.CreatePropertyCompanyResponse;
import com.wuye.points.dto.propertycompany.PropertyCompanyCurrentResponse;
import com.wuye.points.dto.propertycompany.PropertyCompanyDetailResponse;
import com.wuye.points.dto.propertycompany.PropertyCompanyListItemResponse;
import com.wuye.points.dto.propertycompany.UpdatePropertyCompanyRequest;
import com.wuye.points.service.PropertyCompanyService;
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
 * 物业公司模块
 */
@Tag(name = "物业公司模块", description = "物业公司列表、详情、小区列表")
@RestController
@RequestMapping("/property-companies")
@RequiredArgsConstructor
public class PropertyCompanyController {

    private final PropertyCompanyService propertyCompanyService;

    @Operation(summary = "获取当前物业公司配置", description = "获取当前物业公司配置")
    @GetMapping("/current")
    public ApiResponse<PropertyCompanyCurrentResponse> getCurrentConfig() {
        return ApiResponse.success(propertyCompanyService.getCurrentConfig());
    }

    @Operation(summary = "物业公司列表", description = "分页查询物业公司列表")
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

    @Operation(summary = "创建物业公司", description = "创建物业公司")
    @PostMapping
    public ApiResponse<CreatePropertyCompanyResponse> createPropertyCompany(
            @Valid @RequestBody CreatePropertyCompanyRequest request) {
        return ApiResponse.success(propertyCompanyService.createPropertyCompany(request));
    }

    @Operation(summary = "小区列表", description = "获取物业公司下的小区列表")
    @GetMapping("/{id}/communities")
    public ApiResponse<CommunityListResponse> listCommunities(
            @Parameter(description = "物业公司ID", example = "1") @PathVariable String id) {
        return ApiResponse.success(propertyCompanyService.listCommunities(id));
    }

    @Operation(summary = "物业公司详情", description = "获取物业公司详情")
    @GetMapping("/{id}")
    public ApiResponse<PropertyCompanyDetailResponse> getPropertyCompany(
            @Parameter(description = "物业公司ID", example = "1") @PathVariable String id) {
        return ApiResponse.success(propertyCompanyService.getPropertyCompany(id));
    }

    @Operation(summary = "更新物业公司", description = "更新物业公司")
    @PutMapping("/{id}")
    public ApiResponse<PropertyCompanyDetailResponse> updatePropertyCompany(
            @Parameter(description = "物业公司ID", example = "1") @PathVariable String id,
            @Valid @RequestBody UpdatePropertyCompanyRequest request) {
        return ApiResponse.success(propertyCompanyService.updatePropertyCompany(id, request));
    }
}
