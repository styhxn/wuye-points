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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "配送模块", description = "待抢单、抢单、完成配送、状态更新、物业指派")
@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    /**
     * 待抢单列表
     * Auth: Required | Roles: courier
     */
    @Operation(summary = "待抢单列表", description = "分页查询待抢单列表，需登录，角色：courier")
    @GetMapping("/pending")
    public ApiResponse<PageResponse<PendingDeliveryItemResponse>> listPending(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "小区ID") @RequestParam(required = false) String communityId,
            @Parameter(description = "排序字段", example = "-createdAt") @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(deliveryService.listPending(page, pageSize, communityId, sort));
    }

    /**
     * 可抢单列表（与 pending 等价，对齐 docs/03）
     * Auth: Required | Roles: courier
     */
    @Operation(summary = "可抢单列表", description = "分页查询可抢单列表（与 pending 等价），需登录，角色：courier")
    @GetMapping("/available")
    public ApiResponse<PageResponse<PendingDeliveryItemResponse>> listAvailable(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "小区ID") @RequestParam(required = false) String communityId,
            @Parameter(description = "排序字段", example = "-createdAt") @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(deliveryService.listPending(page, pageSize, communityId, sort));
    }

    /**
     * 我的配送任务
     * Auth: Required | Roles: courier
     */
    @Operation(summary = "我的配送任务", description = "分页查询我的配送任务列表，需登录，角色：courier")
    @GetMapping("/my")
    public ApiResponse<PageResponse<MyDeliveryItemResponse>> listMyDeliveries(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status,
            @Parameter(description = "排序字段", example = "-createdAt") @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(deliveryService.listMyDeliveries(page, pageSize, status, sort));
    }

    /**
     * 快递员抢单
     * Auth: Required | Roles: courier
     */
    @Operation(summary = "快递员抢单", description = "快递员抢单，需登录，角色：courier")
    @PostMapping("/{id}/grab")
    public ApiResponse<GrabDeliveryResponse> grabDelivery(
            @Parameter(description = "配送单ID", example = "1") @PathVariable String id) {
        return ApiResponse.success(deliveryService.grabDelivery(id));
    }

    /**
     * 完成配送
     * Auth: Required | Roles: courier（仅接单人）
     */
    @Operation(summary = "完成配送", description = "完成配送，需登录，角色：courier（仅接单人）")
    @PostMapping("/{id}/complete")
    public ApiResponse<CompleteDeliveryResponse> completeDelivery(
            @Parameter(description = "配送单ID", example = "1") @PathVariable String id,
            @Valid @RequestBody(required = false) CompleteDeliveryRequest request) {
        return ApiResponse.success(deliveryService.completeDelivery(id, request));
    }

    /**
     * 更新配送状态
     * Auth: Required | Roles: courier, property_admin
     */
    @Operation(summary = "更新配送状态", description = "更新配送状态，需登录，角色：courier, property_admin")
    @PatchMapping("/{id}/status")
    public ApiResponse<UpdateDeliveryStatusResponse> updateStatus(
            @Parameter(description = "配送单ID", example = "1") @PathVariable String id,
            @Valid @RequestBody UpdateDeliveryStatusRequest request) {
        return ApiResponse.success(deliveryService.updateStatus(id, request));
    }

    /**
     * 商家确认自配送达
     * Auth: Required | Roles: merchant（仅本单商家，且仅 carrier_type = merchant 的配送单）
     */
    @Operation(summary = "商家确认自配送达",
            description = "商家自配单（carrier_type=merchant）送达确认：配送单置 completed，订单同步置 completed。"
                    + "平台配送单请走 POST /deliveries/{id}/complete。角色：merchant")
    @PostMapping("/{id}/merchant-confirm")
    public ApiResponse<CompleteDeliveryResponse> merchantConfirm(
            @Parameter(description = "配送单ID", example = "dlv_xxx") @PathVariable String id,
            @Valid @RequestBody(required = false) CompleteDeliveryRequest request) {
        return ApiResponse.success(deliveryService.merchantConfirm(id, request));
    }

    /**
     * 物业指派配送
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @Operation(summary = "物业指派配送", description = "物业指派配送，需登录，角色：property_admin, platform_admin")
    @PostMapping("/{id}/assign")
    public ApiResponse<AssignDeliveryResponse> assignDelivery(
            @Parameter(description = "配送单ID", example = "1") @PathVariable String id,
            @Valid @RequestBody AssignDeliveryRequest request) {
        return ApiResponse.success(deliveryService.assignDelivery(id, request));
    }
}
