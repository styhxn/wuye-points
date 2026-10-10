package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.delivery.AdminDeliveryOverviewResponse;
import com.wuye.points.dto.delivery.CourierOptionResponse;
import com.wuye.points.dto.delivery.DeliveryCapacityResponse;
import com.wuye.points.dto.order.OrderListItemResponse;
import com.wuye.points.service.AdminDeliveryService;
import com.wuye.points.service.OrderService;
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
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDeliveryController {

    private final AdminDeliveryService adminDeliveryService;
    private final OrderService orderService;

    @Operation(summary = "送货管理概览", description = "获取送货管理概览数据（快递员状态、订单监控、运力分析）。角色: property_admin, platform_admin")
    @GetMapping("/deliveries/overview")
    public ApiResponse<AdminDeliveryOverviewResponse> getOverview(
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminDeliveryService.getOverview(propertyCompanyId));
    }

    @Operation(summary = "运力效能分析", description = "获取配送高峰与快递员响应曲线。角色: property_admin, platform_admin")
    @GetMapping("/deliveries/capacity")
    public ApiResponse<DeliveryCapacityResponse> getCapacityAnalysis(
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "时间维度", example = "24h") @RequestParam(defaultValue = "24h") String dimension) {
        return ApiResponse.success(adminDeliveryService.getCapacityAnalysis(propertyCompanyId, dimension));
    }

    @Operation(summary = "配送员列表（管理端）",
            description = "与 §9.8 GET /deliveries/couriers 同源。角色: property_admin, platform_admin")
    @GetMapping("/couriers")
    public ApiResponse<PageResponse<CourierOptionResponse>> listCouriers(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminDeliveryService.listCouriers(propertyCompanyId, page, pageSize));
    }

    @Operation(summary = "配送相关订单列表（管理端）",
            description = "分页查询订单（底层与订单列表同源筛选）。角色: property_admin, platform_admin")
    @GetMapping("/delivery-orders")
    public ApiResponse<PageResponse<OrderListItemResponse>> listDeliveryOrders(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "订单状态筛选") @RequestParam(required = false) String orderStatus,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "排序字段", example = "-createdAt") @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(orderService.listOrders(
                page, pageSize, orderStatus, null, null, propertyCompanyId, startDate, endDate, keyword, sort));
    }
}
