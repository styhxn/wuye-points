package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.delivery.AdminDeliveryOverviewResponse;
import com.wuye.points.dto.delivery.DeliveryCapacityResponse;
import com.wuye.points.service.AdminDeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 — 送货管理
 */
@Tag(name = "管理端 — 送货管理", description = "快递员管理、订单监控")
@RestController
@RequestMapping("/admin/deliveries")
@RequiredArgsConstructor
public class AdminDeliveryController {

    private final AdminDeliveryService adminDeliveryService;

    @Operation(summary = "送货管理概览", description = "获取送货管理概览数据（快递员状态、订单监控、运力分析）。角色: property_admin, platform_admin")
    @GetMapping("/overview")
    public ApiResponse<AdminDeliveryOverviewResponse> getOverview(
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminDeliveryService.getOverview(propertyCompanyId));
    }

    @Operation(summary = "运力效能分析", description = "获取配送高峰与快递员响应曲线。角色: property_admin, platform_admin")
    @GetMapping("/capacity")
    public ApiResponse<DeliveryCapacityResponse> getCapacityAnalysis(
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "时间维度", example = "24h") @RequestParam(defaultValue = "24h") String dimension) {
        return ApiResponse.success(adminDeliveryService.getCapacityAnalysis(propertyCompanyId, dimension));
    }
}
