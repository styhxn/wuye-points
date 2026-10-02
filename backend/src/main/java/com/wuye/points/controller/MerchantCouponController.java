package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.coupon.CouponTemplateResponse;
import com.wuye.points.dto.coupon.GrantCouponRequest;
import com.wuye.points.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 优惠券模块（商家端）：查看本商家券、定向发放。
 */
@Tag(name = "优惠券模块（商家端）", description = "本商家优惠券、定向发放")
@RestController
@RequestMapping("/merchant/coupons")
@RequiredArgsConstructor
public class MerchantCouponController {

    private final CouponService couponService;

    @Operation(summary = "本商家优惠券列表", description = "Auth: Required | Roles: merchant")
    @GetMapping
    public ApiResponse<List<CouponTemplateResponse>> list() {
        return ApiResponse.success(couponService.listMerchantTemplates());
    }

    @Operation(summary = "定向发放本商家优惠券", description = "Auth: Required | Roles: merchant")
    @PostMapping("/grant")
    public ApiResponse<Map<String, Integer>> grant(@Valid @RequestBody GrantCouponRequest request) {
        int granted = couponService.grant(request);
        return ApiResponse.success(Map.of("grantedCount", granted));
    }
}