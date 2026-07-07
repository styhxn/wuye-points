package com.wuye.points.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "订单模块", description = "创建订单、支付、取消、确认收货、订单列表")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "创建订单", description = "创建订单，需登录，角色：resident")
    @PostMapping
    public ApiResponse<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.success(orderService.createOrder(request));
    }

    /**
     * 支付订单
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "支付订单", description = "支付订单，需登录，角色：resident")
    @PostMapping("/{id}/pay")
    public ApiResponse<PayOrderResponse> payOrder(
            @Parameter(description = "订单ID", example = "1") @PathVariable String id,
            @Valid @RequestBody PayOrderRequest request) {
        return ApiResponse.success(orderService.payOrder(id, request));
    }

    /**
     * 我的订单
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "我的订单", description = "分页查询我的订单列表，需登录，角色：resident")
    @GetMapping("/my")
    public ApiResponse<PageResponse<OrderListItemResponse>> listMyOrders(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "订单状态") @RequestParam(required = false) String orderStatus,
            @Parameter(description = "商家ID") @RequestParam(required = false) String merchantId,
            @Parameter(description = "开始日期", example = "2026-01-01") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期", example = "2026-12-31") @RequestParam(required = false) String endDate,
            @Parameter(description = "关键词（订单号或商品名）") @RequestParam(required = false) String keyword,
            @Parameter(description = "排序字段", example = "-createdAt") @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(orderService.listMyOrders(
                page, pageSize, orderStatus, merchantId, startDate, endDate, keyword, sort));
    }

    /**
     * 订单列表（商家/管理员）
     * Auth: Required | Roles: merchant, property_admin, platform_admin
     */
    @Operation(summary = "订单列表", description = "分页查询订单列表（商家/管理员），需登录，角色：merchant, property_admin, platform_admin")
    @GetMapping
    public ApiResponse<PageResponse<OrderListItemResponse>> listOrders(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "订单状态") @RequestParam(required = false) String orderStatus,
            @Parameter(description = "商家ID") @RequestParam(required = false) String merchantId,
            @Parameter(description = "业主ID") @RequestParam(required = false) String residentId,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "开始日期", example = "2026-01-01") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期", example = "2026-12-31") @RequestParam(required = false) String endDate,
            @Parameter(description = "关键词（订单号或商品名）") @RequestParam(required = false) String keyword,
            @Parameter(description = "排序字段", example = "-createdAt") @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(orderService.listOrders(
                page, pageSize, orderStatus, merchantId, residentId,
                propertyCompanyId, startDate, endDate, keyword, sort));
    }

    /**
     * 订单详情
     * Auth: Required | Roles: resident（本人）, property_admin, merchant, platform_admin
     */
    @Operation(summary = "订单详情", description = "获取订单详情，需登录，角色：resident（本人）, property_admin, merchant, platform_admin")
    @GetMapping("/{id}")
    public ApiResponse<OrderDetailResponse> getOrder(
            @Parameter(description = "订单ID", example = "1") @PathVariable String id) {
        return ApiResponse.success(orderService.getOrder(id));
    }

    /**
     * 取消订单
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "取消订单", description = "取消订单，需登录，角色：resident")
    @PostMapping("/{id}/cancel")
    public ApiResponse<CancelOrderResponse> cancelOrder(
            @Parameter(description = "订单ID", example = "1") @PathVariable String id,
            @Valid @RequestBody CancelOrderRequest request) {
        return ApiResponse.success(orderService.cancelOrder(id, request));
    }

    /**
     * 确认收货
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "确认收货", description = "确认收货，需登录，角色：resident")
    @PostMapping("/{id}/confirm")
    public ApiResponse<ConfirmOrderResponse> confirmOrder(
            @Parameter(description = "订单ID", example = "1") @PathVariable String id) {
        return ApiResponse.success(orderService.confirmOrder(id));
    }

    /**
     * 更新订单状态
     * Auth: Required | Roles: merchant, courier
     */
    @Operation(summary = "更新订单状态", description = "更新订单状态，需登录，角色：merchant, courier")
    @PatchMapping("/{id}/status")
    public ApiResponse<UpdateOrderStatusResponse> updateStatus(
            @Parameter(description = "订单ID", example = "1") @PathVariable String id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ApiResponse.success(orderService.updateStatus(id, request));
    }
}
