package com.wuye.points.controller;

import com.wuye.points.dto.admin.coordinator.AppointCoordinatorRequest;
import com.wuye.points.dto.admin.coordinator.CoordinatorDetailResponse;
import com.wuye.points.dto.admin.coordinator.CoordinatorListItemResponse;
import com.wuye.points.dto.admin.coordinator.UpdateCoordinatorRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminCoordinatorService;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统筹管理模块（管理端）
 */
@Tag(name = "管理端 — 统筹管理", description = "统筹负责人列表、指派、详情、更新、撤销")
@RestController
@RequestMapping("/admin/coordinators")
@RequiredArgsConstructor
public class AdminCoordinatorController {

    private final AdminCoordinatorService adminCoordinatorService;

    @Operation(summary = "统筹负责人列表", description = "获取统筹负责人列表。角色: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<PageResponse<CoordinatorListItemResponse>> listCoordinators(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "统筹负责人状态") @RequestParam(required = false) String status,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminCoordinatorService.listCoordinators(
                page, pageSize, keyword, status, propertyCompanyId));
    }

    @Operation(summary = "指派统筹负责人", description = "指派统筹负责人。角色: property_admin, platform_admin")
    @PostMapping
    public ApiResponse<CoordinatorDetailResponse> appointCoordinator(
            @Valid @RequestBody AppointCoordinatorRequest request) {
        return ApiResponse.success(adminCoordinatorService.appointCoordinator(request));
    }

    @Operation(summary = "统筹负责人详情", description = "获取统筹负责人详情。角色: property_admin, platform_admin")
    @GetMapping("/{id}")
    public ApiResponse<CoordinatorDetailResponse> getCoordinator(
            @Parameter(description = "资源ID") @PathVariable String id) {
        return ApiResponse.success(adminCoordinatorService.getCoordinator(id));
    }

    @Operation(summary = "更新统筹负责人", description = "更新统筹负责人信息。角色: property_admin, platform_admin")
    @PutMapping("/{id}")
    public ApiResponse<CoordinatorDetailResponse> updateCoordinator(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Valid @RequestBody UpdateCoordinatorRequest request) {
        return ApiResponse.success(adminCoordinatorService.updateCoordinator(id, request));
    }

    @Operation(summary = "撤销统筹负责人",
            description = "撤销统筹负责人；存在启用的下级板块负责人时返回 400 并提示数量。角色: property_admin, platform_admin")
    @DeleteMapping("/{id}")
    public ApiResponse<CoordinatorDetailResponse> revokeCoordinator(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Parameter(description = "撤销原因（可选）") @RequestParam(required = false) String reason) {
        return ApiResponse.success(adminCoordinatorService.revokeCoordinator(id, reason));
    }
}
