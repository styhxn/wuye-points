package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.resident.MyCoinsResponse;
import com.wuye.points.dto.resident.MyPointsResponse;
import com.wuye.points.dto.resident.ResidentDetailResponse;
import com.wuye.points.dto.resident.ResidentListItemResponse;
import com.wuye.points.dto.resident.ResidentStatusResponse;
import com.wuye.points.dto.resident.UpdateResidentRequest;
import com.wuye.points.dto.resident.UpdateResidentStatusRequest;
import com.wuye.points.service.ResidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 业主/住户模块
 */
@RestController
@RequestMapping("/residents")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentService residentService;

    /**
     * 业主列表（管理员）
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @GetMapping
    public ApiResponse<PageResponse<ResidentListItemResponse>> listResidents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String communityId,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String propertyCompanyId,
            @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(residentService.listResidents(
                page, pageSize, keyword, communityId, building, status, role, propertyCompanyId, sort));
    }

    /**
     * 我的积分余额
     * Auth: Required | Roles: resident
     */
    @GetMapping("/me/points")
    public ApiResponse<MyPointsResponse> getMyPoints() {
        return ApiResponse.success(residentService.getMyPoints());
    }

    /**
     * 我的物业币余额
     * Auth: Required | Roles: resident
     */
    @GetMapping("/me/coins")
    public ApiResponse<MyCoinsResponse> getMyCoins() {
        return ApiResponse.success(residentService.getMyCoins());
    }

    /**
     * 获取当前用户信息
     * Auth: Required
     */
    @GetMapping("/me")
    public ApiResponse<ResidentDetailResponse> getCurrentResident() {
        return ApiResponse.success(residentService.getCurrentResident());
    }

    /**
     * 更新当前用户信息
     * Auth: Required
     */
    @PutMapping("/me")
    public ApiResponse<ResidentDetailResponse> updateCurrentResident(
            @Valid @RequestBody UpdateResidentRequest request) {
        return ApiResponse.success(residentService.updateCurrentResident(request));
    }

    /**
     * 业主详情
     * Auth: Required | Roles: property_admin, platform_admin, resident（仅本人）
     */
    @GetMapping("/{id}")
    public ApiResponse<ResidentDetailResponse> getResident(@PathVariable String id) {
        return ApiResponse.success(residentService.getResident(id));
    }

    /**
     * 更新业主信息
     * Auth: Required | Roles: resident（仅本人）, property_admin
     */
    @PutMapping("/{id}")
    public ApiResponse<ResidentDetailResponse> updateResident(
            @PathVariable String id,
            @Valid @RequestBody UpdateResidentRequest request) {
        return ApiResponse.success(residentService.updateResident(id, request));
    }

    /**
     * 冻结/解冻/禁用
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @PatchMapping("/{id}/status")
    public ApiResponse<ResidentStatusResponse> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateResidentStatusRequest request) {
        return ApiResponse.success(residentService.updateStatus(id, request));
    }
}
