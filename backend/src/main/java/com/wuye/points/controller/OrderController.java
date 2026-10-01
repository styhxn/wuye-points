package com.wuye.points.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.order.CancelOrderRequest;
import com.wuye.points.dto.order.CancelOrderResponse;
import com.wuye.points.dto.order.ChooseFulfillmentRequest;
import com.wuye.points.dto.order.ConfirmOrderResponse;
import com.wuye.points.dto.order.CreateListingOrderRequest;
import com.wuye.points.dto.order.CreateOrderRequest;
import com.wuye.points.dto.order.CreateOrderResponse;
import com.wuye.points.dto.order.OrderDetailResponse;
import com.wuye.points.dto.order.OrderListItemResponse;
import com.wuye.points.dto.order.PayOrderRequest;
import com.wuye.points.dto.order.PayOrderResponse;
import com.wuye.points.dto.order.PreviewListingOrderRequest;
import com.wuye.points.dto.order.PreviewOrderRequest;
import com.wuye.points.dto.order.PreviewOrderResponse;
import com.wuye.points.dto.order.RefundRequestDTO;
import com.wuye.points.dto.order.RefundResponse;
import com.wuye.points.dto.order.SendDeliveryResponse;
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

    @Operation(summary = "向业主商户下单", description = "购买业主商户从一级经销商拿货后上架的商品，订单记录上架ID，完成后结算差价并自动下架。需登录，角色：resident")
    @PostMapping("/from-listing")
    public ApiResponse<CreateOrderResponse> createListingOrder(
            @Valid @RequestBody CreateListingOrderRequest request) {
        return ApiResponse.success(orderService.createListingOrder(request));
    }

    @Operation(summary = "业主上架商品支付试算",
            description = "对业主商户上架商品（listingId）试算可抵扣金额与现金应付（含配送费、满额减免、食堂定向余额），"
                    + "口径与下单 /orders/from-listing 一致。上架商品无商城 productId，不可走普通 /orders/preview。"
                    + "仅试算不落库，支付仍以 POST /orders/from-listing 下单后 POST /orders/{id}/pay 后端校验为准。需登录，角色：resident")
    @PostMapping("/from-listing/preview")
    public ApiResponse<PreviewOrderResponse> previewListingOrder(
            @Valid @RequestBody PreviewListingOrderRequest request) {
        return ApiResponse.success(orderService.previewListingOrder(request));
    }

    @Operation(summary = "申请退货",
            description = "住户对本人订单发起退货申请，订单置为 refunding，等待物业/平台管理员审核（§管理员退货审核）。需登录，角色：resident")
    @PostMapping("/{id}/refund-request")
    public ApiResponse<RefundResponse> requestRefund(
            @Parameter(description = "订单ID", example = "ord_xxx") @PathVariable String id,
            @Valid @RequestBody RefundRequestDTO request) {
        return ApiResponse.success(orderService.requestRefund(id, request));
    }

    /**
     * 订单支付试算
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "订单支付试算",
            description = "按物业币商城规则试算可抵扣金额与现金应付（含配送费、满额减免、食堂定向余额）。"
                    + "仅试算不落库，支付仍以 POST /orders/{id}/pay 后端校验为准。需登录，角色：resident")
    @PostMapping("/preview")
    public ApiResponse<PreviewOrderResponse> previewOrder(
            @Valid @RequestBody PreviewOrderRequest request) {
        return ApiResponse.success(orderService.previewOrder(request));
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
    @Operation(summary = "商家选择履约方式",
            description = "商家在订单处于 pending_choice 时选择履约方式：merchant_self=商家自配 / courier_hall=平台配送（发布到抢单大厅）。"
                    + "重复提交同一方式幂等成功；已选其它方式返回 70025。角色: merchant")
    @PostMapping("/{id}/choose-fulfillment")
    public ApiResponse<SendDeliveryResponse> chooseFulfillment(
            @Parameter(description = "订单ID", example = "ord_xxx") @PathVariable String id,
            @Valid @RequestBody ChooseFulfillmentRequest request) {
        return ApiResponse.success(orderService.chooseFulfillment(id, request));
    }

    @Operation(summary = "商家发送配送任务",
            description = "商家将已支付订单发布到配送抢单大厅（等价于选择 courier_hall）。"
                    + "支付成功时后端已自动创建配送单，本接口幂等返回 alreadyExists=true。角色: merchant")
    @PostMapping("/{id}/send-delivery")
    public ApiResponse<SendDeliveryResponse> sendDelivery(
            @Parameter(description = "订单ID", example = "ord_xxx") @PathVariable String id) {
        return ApiResponse.success(orderService.sendDelivery(id));
    }

    @Operation(summary = "更新订单状态", description = "更新订单状态，需登录，角色：merchant, courier")
    @PatchMapping("/{id}/status")
    public ApiResponse<UpdateOrderStatusResponse> updateStatus(
            @Parameter(description = "订单ID", example = "1") @PathVariable String id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ApiResponse.success(orderService.updateStatus(id, request));
    }
}
