package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.coupon.AvailableCouponResponse;
import com.wuye.points.dto.coupon.CouponTemplateResponse;
import com.wuye.points.dto.coupon.UserCouponResponse;
import com.wuye.points.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券模块（用户端）
 */
@Tag(name = "优惠券模块（用户端）", description = "领券中心、领取、我的券、下单可用券")
@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "领券中心", description = "可领取的优惠券列表（平台券+本物业商家券）。Auth: Required | Roles: resident")
    @GetMapping("/center")
    public ApiResponse<List<CouponTemplateResponse>> listCenter(
            @Parameter(description = "商家ID（可选，仅看某商家券）") @RequestParam(required = false) String merchantId) {
        return ApiResponse.success(couponService.listClaimable(merchantId));
    }

    @Operation(summary = "领取优惠券", description = "自助领取。Auth: Required | Roles: resident")
    @PostMapping("/{templateId}/claim")
    public ApiResponse<UserCouponResponse> claim(
            @Parameter(description = "券模板ID") @PathVariable String templateId) {
        return ApiResponse.success(couponService.claim(templateId));
    }

    @Operation(summary = "我的优惠券", description = "my coupons。Auth: Required | Roles: resident")
    @GetMapping("/my")
    public ApiResponse<List<UserCouponResponse>> listMy(
            @Parameter(description = "状态 unused/locked/used/expired，不传=全部") @RequestParam(required = false) String status) {
        return ApiResponse.success(couponService.listMyCoupons(status));
    }

    @Operation(summary = "下单可用券", description = "按订单商品金额试算可用券及抵扣额（不含配送费）。Auth: Required | Roles: resident")
    @GetMapping("/available")
    public ApiResponse<List<AvailableCouponResponse>> listAvailable(
            @Parameter(description = "商家ID", required = true) @RequestParam String merchantId,
            @Parameter(description = "订单商品金额（不含配送费）") @RequestParam(required = false) BigDecimal productAmount) {
        return ApiResponse.success(couponService.listAvailable(merchantId, productAmount));
    }
}