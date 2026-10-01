package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.propertyoperator.CreatePropertyOperatorRequest;
import com.wuye.points.dto.propertyoperator.PropertyOperatorListItemResponse;
import com.wuye.points.dto.propertyoperator.UpdatePropertyOperatorScopeRequest;
import com.wuye.points.dto.propertyoperator.UpdatePropertyOperatorStatusRequest;
import com.wuye.points.service.AdminPropertyOperatorService;
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

@Tag(name = "管理端 — 物业角色层级", description = "物业管理员账号与管辖范围")
@RestController
@RequestMapping("/admin/property-operators")
@RequiredArgsConstructor
public class AdminPropertyOperatorController {

    private final AdminPropertyOperatorService adminPropertyOperatorService;

    @Operation(summary = "创建物业管理员账号", description = "角色: property_admin（含物业领导/物业管理员）, platform_admin")
    @PostMapping
    public ApiResponse<PropertyOperatorListItemResponse> createPropertyOperator(
            @Valid @RequestBody CreatePropertyOperatorRequest request,
            @Parameter(description = "物业公司ID（平台超管必填，PC 端经 Query 自动附加）")
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminPropertyOperatorService.createOperator(request, propertyCompanyId));
    }

    @Operation(summary = "物业管理员列表", description = "角色: property_admin（含物业领导/物业管理员）, platform_admin")
    @GetMapping
    public ApiResponse<PageResponse<PropertyOperatorListItemResponse>> listPropertyOperators(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态") @RequestParam(required = false) String status) {
        return ApiResponse.success(adminPropertyOperatorService.listOperators(page, pageSize, keyword, status));
    }

    @Operation(summary = "更新管辖范围", description = "角色: property_admin（含物业领导）, platform_admin")
    @PutMapping("/{id}/scope")
    public ApiResponse<PropertyOperatorListItemResponse> updateScope(
            @Parameter(description = "管辖范围ID") @PathVariable String id,
            @Valid @RequestBody UpdatePropertyOperatorScopeRequest request) {
        return ApiResponse.success(adminPropertyOperatorService.updateScope(id, request));
    }

    @Operation(summary = "启用/停用物业管理员", description = "角色: property_admin（含物业领导）, platform_admin")
    @PutMapping("/{id}/status")
    public ApiResponse<PropertyOperatorListItemResponse> updateStatus(
            @Parameter(description = "管辖范围ID") @PathVariable String id,
            @Valid @RequestBody UpdatePropertyOperatorStatusRequest request) {
        return ApiResponse.success(adminPropertyOperatorService.updateStatus(id, request));
    }
}
