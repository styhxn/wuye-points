package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.order.OrderListItemResponse;
import com.wuye.points.dto.order.OverrideFulfillmentRequest;
import com.wuye.points.dto.order.RefundAuditRequest;
import com.wuye.points.dto.order.RefundResponse;
import com.wuye.points.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端 — 订单退货审核")
@RestController
@RequestMapping("/admin/orders/refunds")
@RequiredArgsConstructor
public class AdminOrderRefundController {

    private final OrderService orderService;

    @Operation(summary = "退货审核列表",
            description = "查询住户退货申请及审核结果（含待审核/已通过/已驳回）。角色: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<PageResponse<OrderListItemResponse>> listRefundRequests(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "审核状态: refunding(待审核)/refunded(已通过)/refund_rejected(已驳回)")
            @RequestParam(required = false) String status,
            @Parameter(description = "搜索关键词(订单号/收货人/手机号)") @RequestParam(required = false) String keyword,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(orderService.listOrders(
                page, pageSize, status, null, null, propertyCompanyId, null, null, keyword, "createdAt"));
    }

    @Operation(summary = "审核退货申请",
            description = "物业审核通过(approve=执行退款)或不通过(reject=驳回)。角色: property_admin, platform_admin")
    @PostMapping("/{orderId}/audit")
    public ApiResponse<RefundResponse> auditRefund(
            @Parameter(description = "订单ID", example = "ord_xxx") @PathVariable String orderId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "action=approve/reject；reject 时需填 rejectReason")
            @Valid @RequestBody RefundAuditRequest request) {
        return ApiResponse.success(orderService.auditRefund(orderId, request));
    }

    @Operation(summary = "强制改派履约方式",
            description = "管理员强制修改订单履约方式（merchant_self / courier_hall），写入履约选择审计日志留痕。"
                    + "角色: property_admin, platform_admin")
    @PostMapping("/{orderId}/fulfillment-override")
    public ApiResponse<Void> overrideFulfillment(
            @Parameter(description = "订单ID", example = "ord_xxx") @PathVariable String orderId,
            @Valid @RequestBody OverrideFulfillmentRequest request) {
        orderService.overrideFulfillment(orderId, request);
        return ApiResponse.success(null);
    }
}
