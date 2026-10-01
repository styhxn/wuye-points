package com.wuye.points.controller;

import com.wuye.points.dto.activitygrouptier.CreatePricingTierRequest;
import com.wuye.points.dto.activitygrouptier.PricingTierItemResponse;
import com.wuye.points.dto.activitygrouptier.PricingTierListResponse;
import com.wuye.points.dto.activitygrouptier.UpdatePricingTierRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.service.ActivityGroupPricingTierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 活动组多课时价格模块
 */
@Tag(name = "活动组多课时价格模块", description = "活动组课时价格级别的增删改查与批量替换")
@RestController
@RequestMapping("/activity-groups")
@RequiredArgsConstructor
public class ActivityGroupPricingTierController {

    private final ActivityGroupPricingTierService activityGroupPricingTierService;

    @Operation(summary = "价格级别列表", description = "获取指定活动组的全部课时价格级别")
    @GetMapping("/{id}/pricing-tiers")
    public ApiResponse<PricingTierListResponse> listTiers(
            @Parameter(description = "活动组ID") @PathVariable String id) {
        return ApiResponse.success(activityGroupPricingTierService.listTiers(id));
    }

    @Operation(summary = "创建价格级别", description = "为指定活动组新增一个课时价格级别（角色：resident 且为组长）")
    @PostMapping("/{id}/pricing-tiers")
    public ApiResponse<PricingTierItemResponse> createTier(
            @Parameter(description = "活动组ID") @PathVariable String id,
            @Valid @RequestBody CreatePricingTierRequest request) {
        return ApiResponse.success(activityGroupPricingTierService.createTier(id, request));
    }

    @Operation(summary = "更新价格级别", description = "更新指定价格级别（角色：resident 且为组长）")
    @PutMapping("/pricing-tiers/{tierId}")
    public ApiResponse<PricingTierItemResponse> updateTier(
            @Parameter(description = "价格级别ID") @PathVariable String tierId,
            @Valid @RequestBody UpdatePricingTierRequest request) {
        return ApiResponse.success(activityGroupPricingTierService.updateTier(tierId, request));
    }

    @Operation(summary = "删除价格级别", description = "删除指定价格级别（角色：resident 且为组长）")
    @DeleteMapping("/pricing-tiers/{tierId}")
    public ApiResponse<Void> deleteTier(
            @Parameter(description = "价格级别ID") @PathVariable String tierId) {
        activityGroupPricingTierService.deleteTier(tierId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "批量替换价格级别", description = "用传入列表整体替换指定活动组的全部价格级别（角色：resident 且为组长）")
    @PutMapping("/{id}/pricing-tiers")
    public ApiResponse<PricingTierListResponse> batchReplaceTiers(
            @Parameter(description = "活动组ID") @PathVariable String id,
            @Valid @RequestBody List<CreatePricingTierRequest> tiers) {
        return ApiResponse.success(activityGroupPricingTierService.batchReplaceTiers(id, tiers));
    }
}
