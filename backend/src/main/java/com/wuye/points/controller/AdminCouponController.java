package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.coupon.CouponTemplateResponse;
import com.wuye.points.dto.coupon.CreateCouponTemplateRequest;
import com.wuye.points.dto.coupon.GrantCouponRequest;
import com.wuye.points.dto.coupon.UpdateCouponTemplateRequest;
import com.wuye.points.service.CouponService;
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

import java.util.Map;

/**
 * 优惠券模块（管理端）
 */
@Tag(name = "优惠券模块（管理端）", description = "券模板 CRUD、启停、定向发放")
@RestController
@RequestMapping("/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    @Operation(summary = "券模板列表", description = "Roles: platform_admin, property_admin")
    @GetMapping
    public ApiResponse<PageResponse<CouponTemplateResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "适用范围 platform/merchant") @RequestParam(required = false) String scope,
            @Parameter(description = "状态 active/inactive") @RequestParam(required = false) String status,
            @Parameter(description = "商家ID") @RequestParam(required = false) String merchantId,
            @Parameter(description = "物业公司ID（platform_admin 可用）") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(couponService.listTemplates(
                page, pageSize, scope, status, merchantId, propertyCompanyId));
    }

    @Operation(summary = "券模板详情", description = "Roles: platform_admin, property_admin")
    @GetMapping("/{id}")
    public ApiResponse<CouponTemplateResponse> detail(
            @Parameter(description = "券模板ID") @PathVariable String id) {
        return ApiResponse.success(couponService.getTemplate(id));
    }

    @Operation(summary = "创建券模板", description = "Roles: platform_admin, property_admin；平台券仅 platform_admin 可创建")
    @PostMapping
    public ApiResponse<CouponTemplateResponse> create(
            @Valid @RequestBody CreateCouponTemplateRequest request) {
        return ApiResponse.success(couponService.createTemplate(request));
    }

    @Operation(summary = "更新券模板", description = "Roles: platform_admin, property_admin；已发放的券不可改面额/门槛")
    @PutMapping("/{id}")
    public ApiResponse<CouponTemplateResponse> update(
            @Parameter(description = "券模板ID") @PathVariable String id,
            @Valid @RequestBody UpdateCouponTemplateRequest request) {
        return ApiResponse.success(couponService.updateTemplate(id, request));
    }

    @Operation(summary = "启用/停用券模板", description = "Roles: platform_admin, property_admin")
    @PutMapping("/{id}/status")
    public ApiResponse<CouponTemplateResponse> updateStatus(
            @Parameter(description = "券模板ID") @PathVariable String id,
            @RequestBody Map<String, String> body) {
        return ApiResponse.success(couponService.updateTemplateStatus(id, body.get("status")));
    }

    @Operation(summary = "定向发放优惠券", description = "向指定居民发放。Roles: platform_admin, property_admin")
    @PostMapping("/grant")
    public ApiResponse<Map<String, Integer>> grant(@Valid @RequestBody GrantCouponRequest request) {
        int granted = couponService.grant(request);
        return ApiResponse.success(Map.of("grantedCount", granted));
    }
}