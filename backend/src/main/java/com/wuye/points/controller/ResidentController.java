package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.resident.CreateResidentRequest;
import com.wuye.points.dto.resident.DeleteAccountResponse;
import com.wuye.points.dto.resident.MyCoinsResponse;
import com.wuye.points.dto.resident.MyPointsResponse;
import com.wuye.points.dto.resident.ResidentDetailResponse;
import com.wuye.points.dto.resident.ResidentListItemResponse;
import com.wuye.points.dto.resident.ResidentStatusResponse;
import com.wuye.points.dto.resident.UpdateResidentRequest;
import com.wuye.points.dto.resident.UpdateResidentStatusRequest;
import com.wuye.points.service.ResidentService;
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

/**
 * 业主/住户模块
 */
@Tag(name = "业主/住户模块", description = "业主列表、创建、详情、状态管理、积分/物业币余额")
@RestController
@RequestMapping("/residents")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentService residentService;

    /**
     * 管理员创建住户
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @Operation(summary = "管理员创建住户", description = "管理员创建住户，需登录，角色：property_admin, platform_admin")
    @PostMapping
    public ApiResponse<ResidentDetailResponse> createResident(
            @Valid @RequestBody CreateResidentRequest request) {
        return ApiResponse.success(residentService.createResident(request));
    }

    /**
     * 业主列表（管理员）
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @Operation(summary = "业主列表", description = "分页查询业主列表（管理员），需登录，角色：property_admin, platform_admin")
    @GetMapping
    public ApiResponse<PageResponse<ResidentListItemResponse>> listResidents(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "小区ID") @RequestParam(required = false) String communityId,
            @Parameter(description = "楼栋") @RequestParam(required = false) String building,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status,
            @Parameter(description = "角色筛选") @RequestParam(required = false) String role,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "排序字段", example = "-createdAt") @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(residentService.listResidents(
                page, pageSize, keyword, communityId, building, status, role, propertyCompanyId, sort));
    }

    /**
     * 我的积分余额
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "我的积分余额", description = "获取我的积分余额，需登录，角色：resident")
    @GetMapping("/me/points")
    public ApiResponse<MyPointsResponse> getMyPoints() {
        return ApiResponse.success(residentService.getMyPoints());
    }

    /**
     * 我的物业币余额
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "我的物业币余额", description = "获取我的物业币余额，需登录，角色：resident")
    @GetMapping("/me/coins")
    public ApiResponse<MyCoinsResponse> getMyCoins() {
        return ApiResponse.success(residentService.getMyCoins());
    }

    /**
     * 获取当前用户信息
     * Auth: Required
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前用户信息，需登录")
    @GetMapping("/me")
    public ApiResponse<ResidentDetailResponse> getCurrentResident() {
        return ApiResponse.success(residentService.getCurrentResident());
    }

    /**
     * 更新当前用户信息
     * Auth: Required
     */
    @Operation(summary = "更新当前用户信息", description = "更新当前用户信息，需登录")
    @PutMapping("/me")
    public ApiResponse<ResidentDetailResponse> updateCurrentResident(
            @Valid @RequestBody UpdateResidentRequest request) {
        return ApiResponse.success(residentService.updateCurrentResident(request));
    }

    /**
     * 业主详情
     * Auth: Required | Roles: property_admin, platform_admin, resident（仅本人）
     */
    @Operation(summary = "业主详情", description = "获取业主详情，需登录，角色：property_admin, platform_admin, resident（仅本人）")
    @GetMapping("/{id}")
    public ApiResponse<ResidentDetailResponse> getResident(
            @Parameter(description = "业主ID", example = "1") @PathVariable String id) {
        return ApiResponse.success(residentService.getResident(id));
    }

    /**
     * 更新业主信息
     * Auth: Required | Roles: resident（仅本人）, property_admin, platform_admin
     */
    @Operation(summary = "更新业主信息", description = "更新业主信息，需登录，角色：resident（仅本人）, property_admin, platform_admin")
    @PutMapping("/{id}")
    public ApiResponse<ResidentDetailResponse> updateResident(
            @Parameter(description = "业主ID", example = "1") @PathVariable String id,
            @Valid @RequestBody UpdateResidentRequest request) {
        return ApiResponse.success(residentService.updateResident(id, request));
    }

    /**
     * 冻结/解冻/禁用
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @Operation(summary = "冻结/解冻/禁用", description = "冻结/解冻/禁用业主，需登录，角色：property_admin, platform_admin")
    @PatchMapping("/{id}/status")
    public ApiResponse<ResidentStatusResponse> updateStatus(
            @Parameter(description = "业主ID", example = "1") @PathVariable String id,
            @Valid @RequestBody UpdateResidentStatusRequest request) {
        return ApiResponse.success(residentService.updateStatus(id, request));
    }

    /**
     * 删除住户（软删除）
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @Operation(summary = "删除住户", description = "删除住户（软删除），需登录，角色：property_admin, platform_admin")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteResident(
            @Parameter(description = "业主ID", example = "1") @PathVariable String id) {
        residentService.deleteResident(id);
        return ApiResponse.success(null);
    }

    /**
     * 申请注销账号
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "申请注销账号", description = "申请注销账号，需登录，角色：resident")
    @PostMapping("/me/delete-request")
    public ApiResponse<DeleteAccountResponse> requestAccountDeletion() {
        return ApiResponse.success(residentService.requestAccountDeletion());
    }
}
