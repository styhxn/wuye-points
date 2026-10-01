package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.order.OverrideFulfillmentRequest;
import com.wuye.points.dto.order.SendDeliveryResponse;
import com.wuye.points.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 — 订单履约方式强制改派（§68.4）
 */
@Tag(name = "管理端 — 订单履约改派")
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderFulfillmentController {

    private final OrderService orderService;

    @Operation(summary = "强制改派履约方式",
            description = "管理员强制修改订单履约方式（merchant_self / courier_hall）："
                    + "同步重建配送单并写入履约选择审计日志留痕；已结束订单不可改派。角色: property_admin, platform_admin")
    @PostMapping("/{orderId}/override-fulfillment")
    public ApiResponse<SendDeliveryResponse> overrideFulfillment(
            @Parameter(description = "订单ID", example = "ord_xxx") @PathVariable String orderId,
            @Valid @RequestBody OverrideFulfillmentRequest request) {
        return ApiResponse.success(orderService.overrideFulfillment(orderId, request));
    }
}
