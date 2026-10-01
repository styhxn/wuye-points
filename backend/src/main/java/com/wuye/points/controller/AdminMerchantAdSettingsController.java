package com.wuye.points.controller;

import com.wuye.points.dto.admin.merchantad.MerchantAdSettingsResponse;
import com.wuye.points.dto.admin.merchantad.UpdateMerchantAdSettingsRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.service.MerchantAdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端 — 广告位配置", description = "广告单日价 / 免费额度开关 / 每周免费条数")
@RestController
@RequestMapping("/admin/merchant-ad-settings")
@RequiredArgsConstructor
public class AdminMerchantAdSettingsController {

    private final MerchantAdService merchantAdService;

    @Operation(summary = "查询广告位配置", description = "角色: platform_admin, property_admin")
    @GetMapping
    public ApiResponse<MerchantAdSettingsResponse> getSettings() {
        return ApiResponse.success(merchantAdService.getAdSettings());
    }

    @Operation(summary = "更新广告位配置", description = "角色: platform_admin, property_admin")
    @PutMapping
    public ApiResponse<MerchantAdSettingsResponse> updateSettings(
            @Valid @RequestBody UpdateMerchantAdSettingsRequest request) {
        return ApiResponse.success(merchantAdService.updateAdSettings(request));
    }
}
