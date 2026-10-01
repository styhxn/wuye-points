package com.wuye.points.controller;

import com.wuye.points.dto.admin.merchant.CommunityDistanceItemResponse;
import com.wuye.points.dto.admin.merchant.UpdateMerchantCommunityDistancesRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.service.MerchantCommunityDistanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端 — 商家-小区距离配置（替代 GPS 算距）
 */
@Tag(name = "管理端 — 商家-小区距离配置", description = "管理端为商家×小区配置距离（distanceKm），商品/商家列表据此展示「距您小区x.xkm」")
@RestController
@RequestMapping("/admin/merchants/{merchantId}/community-distances")
@RequiredArgsConstructor
public class AdminMerchantCommunityDistanceController {

    private final MerchantCommunityDistanceService merchantCommunityDistanceService;

    @Operation(summary = "查询商家-小区距离配置",
            description = "返回该商家所属物业下全部小区及已配置距离（未配置为 null）。角色: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<List<CommunityDistanceItemResponse>> getCommunityDistances(
            @Parameter(description = "商家ID", example = "mch_xxx") @PathVariable String merchantId) {
        return ApiResponse.success(merchantCommunityDistanceService.getMerchantDistances(merchantId));
    }

    @Operation(summary = "配置商家-小区距离",
            description = "写入/更新商家与各小区的距离；distanceKm 为 null 表示清除该小区配置。角色: property_admin, platform_admin")
    @PutMapping
    public ApiResponse<List<CommunityDistanceItemResponse>> updateCommunityDistances(
            @Parameter(description = "商家ID", example = "mch_xxx") @PathVariable String merchantId,
            @Valid @RequestBody UpdateMerchantCommunityDistancesRequest request) {
        return ApiResponse.success(merchantCommunityDistanceService.updateMerchantDistances(merchantId, request));
    }
}
