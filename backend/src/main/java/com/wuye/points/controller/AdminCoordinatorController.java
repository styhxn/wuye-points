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
import org.springframework.web.bind.annotation.RestController;

/**
 * 统筹管理模块（管理端）
 */
@RestController
@RequestMapping("/admin/coordinators")
@RequiredArgsConstructor
public class AdminCoordinatorController {

    private final AdminCoordinatorService adminCoordinatorService;

    @GetMapping
    public ApiResponse<PageResponse<CoordinatorListItemResponse>> listCoordinators(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminCoordinatorService.listCoordinators(
                page, pageSize, keyword, status, propertyCompanyId));
    }

    @PostMapping
    public ApiResponse<CoordinatorDetailResponse> appointCoordinator(
            @Valid @RequestBody AppointCoordinatorRequest request) {
        return ApiResponse.success(adminCoordinatorService.appointCoordinator(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<CoordinatorDetailResponse> getCoordinator(@PathVariable String id) {
        return ApiResponse.success(adminCoordinatorService.getCoordinator(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<CoordinatorDetailResponse> updateCoordinator(
            @PathVariable String id,
            @Valid @RequestBody UpdateCoordinatorRequest request) {
        return ApiResponse.success(adminCoordinatorService.updateCoordinator(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<CoordinatorDetailResponse> revokeCoordinator(
            @PathVariable String id,
            @RequestParam String reason) {
        return ApiResponse.success(adminCoordinatorService.revokeCoordinator(id, reason));
    }
}
