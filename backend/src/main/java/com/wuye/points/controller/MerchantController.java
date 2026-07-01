package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.merchant.CreateMerchantRequest;
import com.wuye.points.dto.merchant.MerchantDetailResponse;
import com.wuye.points.dto.merchant.MerchantListItemResponse;
import com.wuye.points.dto.merchant.UpdateMerchantRequest;
import com.wuye.points.service.MerchantService;
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
@RestController
@RequestMapping("/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    /**
     * 商家列表（分页/筛选）
     * Auth: Optional | Roles: any
     */
    @GetMapping
    public ApiResponse<PageResponse<MerchantListItemResponse>> listMerchants(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String merchantLevel,
            @RequestParam(required = false) String propertyCompanyId,
            @RequestParam(defaultValue = "-rankOrder") String sort) {
        return ApiResponse.success(merchantService.listMerchants(
                page, pageSize, keyword, category, merchantLevel, propertyCompanyId, sort));
    }

    /**
     * 我的商家
     * Auth: Required | Roles: merchant
     */
    @GetMapping("/my")
    public ApiResponse<MerchantDetailResponse> getMyMerchant() {
        return ApiResponse.success(merchantService.getMyMerchant());
    }

    /**
     * 商家详情
     * Auth: Optional | Roles: any
     */
    @GetMapping("/{id}")
    public ApiResponse<MerchantDetailResponse> getMerchant(@PathVariable String id) {
        return ApiResponse.success(merchantService.getMerchant(id));
    }

    /**
     * 商家入驻申请
     * Auth: Required | Roles: resident
     */
    @PostMapping
    public ApiResponse<MerchantDetailResponse> applyMerchant(
            @Valid @RequestBody CreateMerchantRequest request) {
        return ApiResponse.success(merchantService.applyMerchant(request));
    }

    /**
     * 更新商家信息
     * Auth: Required | Roles: merchant, property_admin, platform_admin
     */
    @PutMapping("/{id}")
    public ApiResponse<MerchantDetailResponse> updateMerchant(
            @PathVariable String id,
            @Valid @RequestBody UpdateMerchantRequest request) {
        return ApiResponse.success(merchantService.updateMerchant(id, request));
    }
}
