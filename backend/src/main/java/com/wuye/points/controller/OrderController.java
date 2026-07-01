package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.order.CancelOrderRequest;
import com.wuye.points.dto.order.CancelOrderResponse;
import com.wuye.points.dto.order.ConfirmOrderResponse;
import com.wuye.points.dto.order.CreateOrderRequest;
import com.wuye.points.dto.order.CreateOrderResponse;
import com.wuye.points.dto.order.OrderDetailResponse;
import com.wuye.points.dto.order.OrderListItemResponse;
import com.wuye.points.dto.order.PayOrderRequest;
import com.wuye.points.dto.order.PayOrderResponse;
import com.wuye.points.dto.order.UpdateOrderStatusRequest;
import com.wuye.points.dto.order.UpdateOrderStatusResponse;
import com.wuye.points.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单模块
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     * Auth: Required | Roles: resident
     */
    @PostMapping
    public ApiResponse<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.success(orderService.createOrder(request));
    }

    /**
     * 支付订单
     * Auth: Required | Roles: resident
     */
    @PostMapping("/{id}/pay")
    public ApiResponse<PayOrderResponse> payOrder(
            @PathVariable String id,
            @Valid @RequestBody PayOrderRequest request) {
        return ApiResponse.success(orderService.payOrder(id, request));
    }

    /**
     * 我的订单
     * Auth: Required | Roles: resident
     */
    @GetMapping("/my")
    public ApiResponse<PageResponse<OrderListItemResponse>> listMyOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(orderService.listMyOrders(
                page, pageSize, orderStatus, merchantId, startDate, endDate, sort));
    }

    /**
     * 订单列表（商家/管理员）
     * Auth: Required | Roles: merchant, property_admin, platform_admin
     */
    @GetMapping
    public ApiResponse<PageResponse<OrderListItemResponse>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) String residentId,
            @RequestParam(required = false) String propertyCompanyId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(orderService.listOrders(
                page, pageSize, orderStatus, merchantId, residentId,
                propertyCompanyId, startDate, endDate, sort));
    }

    /**
     * 订单详情
     * Auth: Required | Roles: resident（本人）, property_admin, merchant, platform_admin
     */
    @GetMapping("/{id}")
    public ApiResponse<OrderDetailResponse> getOrder(@PathVariable String id) {
        return ApiResponse.success(orderService.getOrder(id));
    }

    /**
     * 取消订单
     * Auth: Required | Roles: resident
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<CancelOrderResponse> cancelOrder(
            @PathVariable String id,
            @Valid @RequestBody CancelOrderRequest request) {
        return ApiResponse.success(orderService.cancelOrder(id, request));
    }

    /**
     * 确认收货
     * Auth: Required | Roles: resident
     */
    @PostMapping("/{id}/confirm")
    public ApiResponse<ConfirmOrderResponse> confirmOrder(@PathVariable String id) {
        return ApiResponse.success(orderService.confirmOrder(id));
    }

    /**
     * 更新订单状态
     * Auth: Required | Roles: merchant, courier
     */
    @PatchMapping("/{id}/status")
    public ApiResponse<UpdateOrderStatusResponse> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ApiResponse.success(orderService.updateStatus(id, request));
    }
}
