package com.wuye.points.controller;

import com.wuye.points.dto.admin.dashboard.DashboardOverviewResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端 — 仪表盘", description = "管理端总览数据")
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @Operation(summary = "管理端总览数据", description = "获取管理端总览数据。角色: property_admin, platform_admin")
    @GetMapping("/overview")
    public ApiResponse<DashboardOverviewResponse> getOverview(
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminDashboardService.getOverview(propertyCompanyId));
    }
}
