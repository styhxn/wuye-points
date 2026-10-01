package com.wuye.points.controller;

import com.wuye.points.dto.admin.merchant.AuditMerchantRegistrationRequest;
import com.wuye.points.dto.admin.merchant.AuditMerchantRegistrationResponse;
import com.wuye.points.dto.admin.merchant.KickMerchantRequest;
import com.wuye.points.dto.admin.merchant.KickMerchantResponse;
import com.wuye.points.dto.admin.merchant.SetOfficialRecommendationRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.merchant.ApplymentDetailResponse;
import com.wuye.points.service.AdminMerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端 — 商家审核/踢出/官方推荐", description = "商家入驻审核、商家踢出、微信进件管理、官方推荐设置")
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

    @Operation(summary = "查看商家微信进件详情", description = "进件状态/驳回原因/法人验证链接等。角色: property_admin, platform_admin")
    @GetMapping("/{id}/applyment")
    public ApiResponse<ApplymentDetailResponse> getApplyment(
            @Parameter(description = "商家ID") @PathVariable String id) {
        return ApiResponse.success(adminMerchantService.getApplyment(id));
    }

    @Operation(summary = "手动提交商家进件到微信", description = "商家资料就绪后运营后台触发。角色: property_admin, platform_admin")
    @PostMapping("/{id}/applyment/submit")
    public ApiResponse<ApplymentDetailResponse> submitApplyment(
            @Parameter(description = "商家ID") @PathVariable String id) {
        return ApiResponse.success(adminMerchantService.submitApplyment(id));
    }

    @Operation(summary = "手动刷新商家进件状态", description = "查询微信侧状态并推进本地状态机。角色: property_admin, platform_admin")
    @PostMapping("/{id}/applyment/refresh")
    public ApiResponse<ApplymentDetailResponse> refreshApplyment(
            @Parameter(description = "商家ID") @PathVariable String id) {
        return ApiResponse.success(adminMerchantService.refreshApplyment(id));
    }

    @Operation(summary = "设置/取消官方推荐商家", description = "将商家设为官方推荐或取消，并指定推荐排序。角色: property_admin, platform_admin。与 admin/platform-merchants/{id}/recommend 等价，供商家运营页调用（兼容 POST/PUT）")
    @RequestMapping(value = "/{id}/recommend", method = {RequestMethod.POST, RequestMethod.PUT})
    public ApiResponse<Void> setRecommendation(
            @Parameter(description = "商家ID(m_)") @PathVariable String id,
            @Valid @RequestBody SetOfficialRecommendationRequest request) {
        adminMerchantService.setRecommendation(id, request);
        return ApiResponse.success(null);
    }
}
