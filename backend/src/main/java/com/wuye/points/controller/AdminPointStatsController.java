package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.stats.PointConsumptionStructureResponse;
import com.wuye.points.dto.stats.PointTrendResponse;
import com.wuye.points.service.AdminPointStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * D7 管理端 — 积分统计(真实聚合,无假数据)
 */
@Tag(name = "管理端 — 积分统计", description = "积分月度趋势、消耗结构")
@RestController
@RequestMapping("/admin/points")
@RequiredArgsConstructor
public class AdminPointStatsController {

    private final AdminPointStatsService adminPointStatsService;

    @Operation(summary = "积分月度趋势",
            description = "按月按日聚合发放/消耗。Auth: Required | Roles: property_admin, platform_admin")
    @GetMapping("/trend")
    public ApiResponse<PointTrendResponse> trend(
            @Parameter(description = "月份(yyyy-MM),缺省本月", example = "2026-07")
            @RequestParam(required = false) String month,
            @Parameter(description = "物业公司ID(平台管理员可指定,物业管理员自动限定本物业)")
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminPointStatsService.getMonthlyTrend(month, propertyCompanyId));
    }

    @Operation(summary = "积分消耗结构",
            description = "按业务来源 source 分组聚合消耗。Auth: Required | Roles: property_admin, platform_admin")
    @GetMapping("/consumption-structure")
    public ApiResponse<PointConsumptionStructureResponse> consumptionStructure(
            @Parameter(description = "月份(yyyy-MM),缺省本月", example = "2026-07")
            @RequestParam(required = false) String month,
            @Parameter(description = "物业公司ID(平台管理员可指定,物业管理员自动限定本物业)")
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminPointStatsService.getConsumptionStructure(month, propertyCompanyId));
    }
}
