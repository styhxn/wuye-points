package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.merchant.CreateMerchantRequest;
import com.wuye.points.dto.merchant.MerchantDetailResponse;
import com.wuye.points.dto.merchant.MerchantListItemResponse;
import com.wuye.points.dto.merchant.UpdateDeliveryFeesRequest;
import com.wuye.points.dto.merchant.UpdateMerchantRequest;
import com.wuye.points.dto.order.ChooseFulfillmentRequest;
import com.wuye.points.dto.order.MerchantVerifyRequest;
import com.wuye.points.dto.order.MerchantVerifyResponse;
import com.wuye.points.dto.order.OrderListItemResponse;
import com.wuye.points.dto.order.SendDeliveryResponse;
import com.wuye.points.service.MerchantService;
import com.wuye.points.service.MerchantVerificationService;
import com.wuye.points.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家模块
 */
@Tag(name = "商家模块", description = "商家列表、入驻申请、详情、更新")
@RestController
@RequestMapping("/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;
    private final OrderService orderService;
    private final MerchantVerificationService merchantVerificationService;

    @Operation(summary = "商家选择履约方式",
            description = "商家在订单处于 pending_choice 时选择履约方式：merchant_self=商家自配 / courier_hall=平台配送（抢单大厅）。"
                    + "超时未选由定时任务默认改派平台配送。返回配送单ID与履约状态。角色: merchant")
    @PostMapping("/my/orders/{orderId}/fulfillment")
    public ApiResponse<SendDeliveryResponse> chooseFulfillment(
            @Parameter(description = "订单ID", example = "ord_xxx") @PathVariable String orderId,
            @Valid @RequestBody ChooseFulfillmentRequest request) {
        return ApiResponse.success(orderService.chooseFulfillment(orderId, request));
    }

    @Operation(summary = "商家核实订单",
            description = "团购类订单支付后进入 pending_verification，商家核实通过（进入冻结期）或拒绝（取消并退款）。角色: merchant")
    @PostMapping("/my/orders/{orderId}/verify")
    public ApiResponse<MerchantVerifyResponse> verifyOrder(
            @Parameter(description = "订单ID", example = "ord_xxx") @PathVariable String orderId,
            @Valid @RequestBody MerchantVerifyRequest request) {
        return ApiResponse.success(merchantVerificationService.verifyOrder(orderId, request));
    }

    /**
     * 商家列表（分页/筛选）
     * Auth: Optional | Roles: any
     */
    @Operation(summary = "商家列表", description = "商家列表（分页/筛选），无需鉴权。recommended=仅返回官方/物业推荐的商家（物业推荐）；不传则返回全部审核通过商家（官方全部、物业公司模块）")
    @GetMapping
    public ApiResponse<PageResponse<MerchantListItemResponse>> listMerchants(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "商家分类") @RequestParam(required = false) String category,
            @Parameter(description = "商家等级") @RequestParam(required = false) String merchantLevel,
            @Parameter(description = "审核状态") @RequestParam(required = false) String auditStatus,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "仅官方/物业推荐：true/false，不传返回全部") @RequestParam(required = false) Boolean recommended,
            @Parameter(description = "排序字段", example = "-rankOrder") @RequestParam(defaultValue = "-rankOrder") String sort) {
        return ApiResponse.success(merchantService.listMerchants(
                page, pageSize, keyword, category, merchantLevel, auditStatus, propertyCompanyId, sort, recommended));
    }

    /**
     * 官方/物业推荐商家（物业推荐首页）。
     * Auth: Optional | Roles: any
     * 一次全量返回推荐商家（不受通用列表 pageSize≤100 限制）。
     */
    @Operation(summary = "官方/物业推荐商家列表", description = "物业推荐首页数据：一次返回全部推荐商家，无需分页参数")
    @GetMapping("/official-recommended")
    public ApiResponse<PageResponse<MerchantListItemResponse>> listOfficialRecommended(
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(merchantService.listOfficialRecommended(propertyCompanyId));
    }

    /**
     * 我的商家
     * Auth: Required | Roles: merchant
     */
    @Operation(summary = "我的商家", description = "获取我的商家信息，需登录，角色：merchant")
    @GetMapping("/my")
    public ApiResponse<MerchantDetailResponse> getMyMerchant() {
        return ApiResponse.success(merchantService.getMyMerchant());
    }

    @Operation(summary = "更新我的多档配送费",
            description = "更新当前用户店铺的多档配送费（§7.2.1）。角色: merchant / activity_leader / technician（本人店）/ property_admin / platform_admin")
    @PutMapping("/my/delivery-fees")
    public ApiResponse<MerchantDetailResponse> updateMyDeliveryFees(
            @Valid @RequestBody UpdateDeliveryFeesRequest request) {
        return ApiResponse.success(merchantService.updateMyDeliveryFees(request));
    }

    @Operation(summary = "我的卖货明细", description = "当前商家自身的订单/卖货明细（可按订单状态过滤）。角色: merchant")
    @GetMapping("/my/sales")
    public ApiResponse<PageResponse<OrderListItemResponse>> listMySales(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "订单状态", example = "completed") @RequestParam(required = false) String orderStatus) {
        return ApiResponse.success(orderService.listMyMerchantSales(page, pageSize, orderStatus));
    }

    /**
     * 商家详情
     * Auth: Optional | Roles: any
     */
    @Operation(summary = "商家详情", description = "获取商家详情，无需鉴权")
    @GetMapping("/{id}")
    public ApiResponse<MerchantDetailResponse> getMerchant(
            @Parameter(description = "商家ID", example = "1") @PathVariable String id) {
        return ApiResponse.success(merchantService.getMerchant(id));
    }

    /**
     * 商家入驻申请
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "商家入驻申请", description = "商家入驻申请，需登录，角色：resident")
    @PostMapping
    public ApiResponse<MerchantDetailResponse> applyMerchant(
            @Valid @RequestBody CreateMerchantRequest request) {
        return ApiResponse.success(merchantService.applyMerchant(request));
    }

    /**
     * 更新商家信息
     * Auth: Required | Roles: merchant, property_admin, platform_admin
     */
    @Operation(summary = "更新商家信息",
            description = "更新商家信息，需登录。角色：merchant（仅自己的商家）/ activity_leader（仅自己的组长店）/ technician（仅自己的技工店）/ property_admin, platform_admin")
    @PutMapping("/{id}")
    public ApiResponse<MerchantDetailResponse> updateMerchant(
            @Parameter(description = "商家ID", example = "1") @PathVariable String id,
            @Valid @RequestBody UpdateMerchantRequest request) {
        return ApiResponse.success(merchantService.updateMerchant(id, request));
    }
}
