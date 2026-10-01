package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.merchantad.AddMerchantAdQuotaRequest;
import com.wuye.points.dto.merchantad.MerchantAdQuotaResponse;
import com.wuye.points.service.MerchantAdService;
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
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端 — 商家广告额度", description = "查询/增加商家付费周额度")
@RestController
@RequestMapping("/admin/merchants")
@RequiredArgsConstructor
public class AdminMerchantAdController {

    private final MerchantAdService merchantAdService;

    @Operation(summary = "查询商家当前广告周额度",
            description = "按 merchantId 查询本周额度(只读,无额度返回全0)。Auth: Required | Roles: property_admin, platform_admin")
    @GetMapping("/{merchantId}/ad-quota")
    public ApiResponse<MerchantAdQuotaResponse> getQuota(
            @Parameter(description = "商家ID") @PathVariable String merchantId) {
        return ApiResponse.success(merchantAdService.getQuotaByMerchantId(merchantId));
    }

    @Operation(summary = "增加商家广告周额度", description = "角色: platform_admin, property_admin")
    @PostMapping("/{merchantId}/ad-quota")
    public ApiResponse<MerchantAdQuotaResponse> addQuota(
            @Parameter(description = "商家ID") @PathVariable String merchantId,
            @Valid @RequestBody AddMerchantAdQuotaRequest request) {
        return ApiResponse.success(merchantAdService.addPurchasedQuota(
                merchantId, request.getPurchasedQuota()));
    }
}
