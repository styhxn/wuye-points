package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.merchantad.MerchantAdPackageResponse;
import com.wuye.points.service.MerchantAdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理端 — 商家广告额度包", description = "广告额度包配置")
@RestController
@RequestMapping("/admin/merchant-ad-packages")
@RequiredArgsConstructor
public class AdminMerchantAdPackageController {

    private final MerchantAdService merchantAdService;

    @Operation(summary = "广告额度包配置", description = "角色: platform_admin, property_admin")
    @GetMapping
    public ApiResponse<List<MerchantAdPackageResponse>> listPackages() {
        return ApiResponse.success(merchantAdService.listPackages());
    }
}
