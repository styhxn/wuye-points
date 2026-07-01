package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.delivery.AssignDeliveryRequest;
import com.wuye.points.dto.delivery.AssignDeliveryResponse;
import com.wuye.points.dto.delivery.CompleteDeliveryRequest;
import com.wuye.points.dto.delivery.CompleteDeliveryResponse;
import com.wuye.points.dto.delivery.GrabDeliveryResponse;
import com.wuye.points.dto.delivery.MyDeliveryItemResponse;
import com.wuye.points.dto.delivery.PendingDeliveryItemResponse;
import com.wuye.points.dto.delivery.UpdateDeliveryStatusRequest;
import com.wuye.points.dto.delivery.UpdateDeliveryStatusResponse;
import com.wuye.points.service.DeliveryService;
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
 * 配送模块
 */
@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    /**
     * 待抢单列表
     * Auth: Required | Roles: courier
     */
    @GetMapping("/pending")
    public ApiResponse<PageResponse<PendingDeliveryItemResponse>> listPending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String communityId,
            @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(deliveryService.listPending(page, pageSize, communityId, sort));
    }

    /**
     * 可抢单列表（与 pending 等价，对齐 docs/03）
     * Auth: Required | Roles: courier
     */
    @GetMapping("/available")
    public ApiResponse<PageResponse<PendingDeliveryItemResponse>> listAvailable(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String communityId,
            @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(deliveryService.listPending(page, pageSize, communityId, sort));
    }

    /**
     * 我的配送任务
     * Auth: Required | Roles: courier
     */
    @GetMapping("/my")
    public ApiResponse<PageResponse<MyDeliveryItemResponse>> listMyDeliveries(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(deliveryService.listMyDeliveries(page, pageSize, status, sort));
    }

    /**
     * 快递员抢单
     * Auth: Required | Roles: courier
     */
    @PostMapping("/{id}/grab")
    public ApiResponse<GrabDeliveryResponse> grabDelivery(@PathVariable String id) {
        return ApiResponse.success(deliveryService.grabDelivery(id));
    }

    /**
     * 完成配送
     * Auth: Required | Roles: courier（仅接单人）
     */
    @PostMapping("/{id}/complete")
    public ApiResponse<CompleteDeliveryResponse> completeDelivery(
            @PathVariable String id,
            @Valid @RequestBody(required = false) CompleteDeliveryRequest request) {
        return ApiResponse.success(deliveryService.completeDelivery(id, request));
    }

    /**
     * 更新配送状态
     * Auth: Required | Roles: courier, property_admin
     */
    @PatchMapping("/{id}/status")
    public ApiResponse<UpdateDeliveryStatusResponse> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateDeliveryStatusRequest request) {
        return ApiResponse.success(deliveryService.updateStatus(id, request));
    }

    /**
     * 物业指派配送
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @PostMapping("/{id}/assign")
    public ApiResponse<AssignDeliveryResponse> assignDelivery(
            @PathVariable String id,
            @Valid @RequestBody AssignDeliveryRequest request) {
        return ApiResponse.success(deliveryService.assignDelivery(id, request));
    }
}
