package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.service.ServiceOrderDetailResponse;
import com.wuye.points.dto.service.ServiceOrderListItemResponse;
import com.wuye.points.dto.service.UpdateServiceOrderStatusRequest;
import com.wuye.points.dto.service.UpdateServiceOrderStatusResponse;
import com.wuye.points.service.ServiceOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务订单模块
 */
@Tag(name = "服务订单模块", description = "服务订单列表、详情、状态更新")
@RestController
@RequestMapping("/service-orders")
@RequiredArgsConstructor
public class ServiceOrderController {

    private final ServiceOrderService serviceOrderService;

    @Operation(summary = "服务订单列表", description = "获取服务订单列表（分页/筛选）")
    @GetMapping
    public ApiResponse<PageResponse<ServiceOrderListItemResponse>> listServiceOrders(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "订单状态") @RequestParam(required = false) String orderStatus,
            @Parameter(description = "是否以服务提供者身份查看") @RequestParam(required = false) Boolean asProvider,
            @Parameter(description = "排序字段（-降序，+升序）", example = "-createdAt") @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(serviceOrderService.listServiceOrders(
                page, pageSize, orderStatus, asProvider, sort));
    }

    @Operation(summary = "服务订单详情", description = "获取服务订单详细信息")
    @GetMapping("/{id}")
    public ApiResponse<ServiceOrderDetailResponse> getServiceOrder(
            @Parameter(description = "订单ID") @PathVariable String id) {
        return ApiResponse.success(serviceOrderService.getServiceOrder(id));
    }

    @Operation(summary = "更新服务订单状态", description = "更新指定服务订单的状态")
    @PatchMapping("/{id}/status")
    public ApiResponse<UpdateServiceOrderStatusResponse> updateStatus(
            @Parameter(description = "订单ID") @PathVariable String id,
            @Valid @RequestBody UpdateServiceOrderStatusRequest request) {
        return ApiResponse.success(serviceOrderService.updateStatus(id, request));
    }
}
