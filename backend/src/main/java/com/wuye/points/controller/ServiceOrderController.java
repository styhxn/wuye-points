package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.service.ServiceOrderDetailResponse;
import com.wuye.points.dto.service.ServiceOrderListItemResponse;
import com.wuye.points.dto.service.UpdateServiceOrderStatusRequest;
import com.wuye.points.dto.service.UpdateServiceOrderStatusResponse;
import com.wuye.points.service.ServiceOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务订单模块
 */
@RestController
@RequestMapping("/service-orders")
@RequiredArgsConstructor
public class ServiceOrderController {

    private final ServiceOrderService serviceOrderService;

    @GetMapping
    public ApiResponse<PageResponse<ServiceOrderListItemResponse>> listServiceOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) Boolean asProvider,
            @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(serviceOrderService.listServiceOrders(
                page, pageSize, orderStatus, asProvider, sort));
    }

    @GetMapping("/{id}")
    public ApiResponse<ServiceOrderDetailResponse> getServiceOrder(@PathVariable String id) {
        return ApiResponse.success(serviceOrderService.getServiceOrder(id));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<UpdateServiceOrderStatusResponse> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateServiceOrderStatusRequest request) {
        return ApiResponse.success(serviceOrderService.updateStatus(id, request));
    }
}
