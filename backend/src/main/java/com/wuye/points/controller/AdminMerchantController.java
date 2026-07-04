package com.wuye.points.controller;

import com.wuye.points.dto.admin.merchant.AuditMerchantRegistrationRequest;
import com.wuye.points.dto.admin.merchant.AuditMerchantRegistrationResponse;
import com.wuye.points.dto.admin.merchant.KickMerchantRequest;
import com.wuye.points.dto.admin.merchant.KickMerchantResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.service.AdminMerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端 — 商家审核/踢出", description = "商家入驻审核、商家踢出")
@RestController
@RequestMapping("/admin/merchants")
@RequiredArgsConstructor
public class AdminMerchantController {

    private final AdminMerchantService adminMerchantService;

    @Operation(summary = "审核商家入驻", description = "审核商家入驻申请。角色: property_admin, platform_admin")
    @PostMapping("/{id}/audit")
    public ApiResponse<AuditMerchantRegistrationResponse> auditMerchantRegistration(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Valid @RequestBody AuditMerchantRegistrationRequest request) {
        return ApiResponse.success(adminMerchantService.auditMerchantRegistration(id, request));
    }

    @Operation(summary = "踢出商家", description = "将商家踢出平台。角色: property_admin, platform_admin")
    @PostMapping("/{id}/kick")
    public ApiResponse<KickMerchantResponse> kickMerchant(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Valid @RequestBody KickMerchantRequest request) {
        return ApiResponse.success(adminMerchantService.kickMerchant(id, request));
    }
}
