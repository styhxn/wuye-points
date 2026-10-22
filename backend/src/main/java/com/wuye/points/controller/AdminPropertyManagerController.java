package com.wuye.points.controller;

import com.wuye.points.dto.admin.propertymanager.CreatePropertyManagerRequest;
import com.wuye.points.dto.admin.propertymanager.PropertyManagerDetailResponse;
import com.wuye.points.dto.admin.propertymanager.PropertyManagerListItemResponse;
import com.wuye.points.dto.admin.propertymanager.UpdatePropertyManagerRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminPropertyManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * 物业公司经理管理模块（管理端）
 *
 * <p>物业公司可指派/停用物业经理。经理从单元楼长分成中按比例抽成，抽成比例由管理端设置，
 * 经理本人不可修改自己的比例。
 */
@Tag(name = "管理端 — 物业经理管理", description = "物业经理列表、指派、详情、更新、停用")
@RestController
@RequestMapping("/admin/property-managers")
@RequiredArgsConstructor
public class AdminPropertyManagerController {

    private final AdminPropertyManagerService adminPropertyManagerService;

    @Operation(summary = "物业经理列表", description = "获取物业经理列表。角色: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<PageResponse<PropertyManagerListItemResponse>> listPropertyManagers(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "搜索关键词（住户姓名/手机号）") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态 active/inactive") @RequestParam(required = false) String status,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminPropertyManagerService.listPropertyManagers(
                page, pageSize, keyword, status, propertyCompanyId));
    }

    @Operation(summary = "指派物业经理",
            description = "指定住户为物业公司经理；同一物业公司同一时刻仅允许一名在职经理。角色: property_admin, platform_admin")
    @PostMapping
    public ApiResponse<PropertyManagerDetailResponse> createPropertyManager(
            @Valid @RequestBody CreatePropertyManagerRequest request) {
        return ApiResponse.success(adminPropertyManagerService.createPropertyManager(request));
    }

    @Operation(summary = "物业经理详情", description = "获取物业经理详情。角色: property_admin, platform_admin")
    @GetMapping("/{id}")
    public ApiResponse<PropertyManagerDetailResponse> getPropertyManager(
            @Parameter(description = "经理ID pm_") @PathVariable String id) {
        return ApiResponse.success(adminPropertyManagerService.getPropertyManager(id));
    }

    @Operation(summary = "更新物业经理",
            description = "更新经理抽成比例/状态；经理本人不可修改自己的抽成比例。角色: property_admin, platform_admin")
    @PutMapping("/{id}")
    public ApiResponse<PropertyManagerDetailResponse> updatePropertyManager(
            @Parameter(description = "经理ID pm_") @PathVariable String id,
            @Valid @RequestBody UpdatePropertyManagerRequest request) {
        return ApiResponse.success(adminPropertyManagerService.updatePropertyManager(id, request));
    }

    @Operation(summary = "停用物业经理", description = "停用经理（置为 inactive，楼长分成不再被抽成）。角色: property_admin, platform_admin")
    @DeleteMapping("/{id}")
    public ApiResponse<PropertyManagerDetailResponse> revokePropertyManager(
            @Parameter(description = "经理ID pm_") @PathVariable String id) {
        return ApiResponse.success(adminPropertyManagerService.revokePropertyManager(id));
    }
}
