package com.wuye.points.controller;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.profilereward.CreateRewardConfigRequest;
import com.wuye.points.dto.profilereward.RewardConfigItemResponse;
import com.wuye.points.dto.profilereward.RewardConfigListResponse;
import com.wuye.points.dto.profilereward.StatsShareResponse;
import com.wuye.points.dto.profilereward.TriggerRewardRequest;
import com.wuye.points.dto.profilereward.TriggerRewardResponse;
import com.wuye.points.dto.profilereward.UpdateRewardConfigRequest;
import com.wuye.points.service.ProfileCompletionRewardService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "信息采集积分激励", description = "信息采集奖励配置管理、居民端触发奖励、我的奖励记录")
@RestController
@RequiredArgsConstructor
public class ProfileCompletionRewardController {

    private final ProfileCompletionRewardService profileCompletionRewardService;

    @Operation(summary = "奖励配置列表", description = "查询信息采集奖励配置列表。角色: property_admin, platform_admin")
    @GetMapping("/admin/profile-rewards")
    public ApiResponse<RewardConfigListResponse> listConfigs(
            @Parameter(description = "作用域") @RequestParam(required = false) String scope,
            @Parameter(description = "作用域ID") @RequestParam(required = false) String scopeId) {
        return ApiResponse.success(profileCompletionRewardService.listConfigs(scope, scopeId));
    }

    @Operation(summary = "创建奖励配置", description = "创建信息采集奖励配置。角色: property_admin, platform_admin")
    @PostMapping("/admin/profile-rewards")
    public ApiResponse<RewardConfigItemResponse> createConfig(
            @Valid @RequestBody CreateRewardConfigRequest request) {
        return ApiResponse.success(profileCompletionRewardService.createConfig(request));
    }

    @Operation(summary = "更新奖励配置", description = "更新信息采集奖励配置。角色: property_admin, platform_admin")
    @PutMapping("/admin/profile-rewards/{id}")
    public ApiResponse<RewardConfigItemResponse> updateConfig(
            @Parameter(description = "配置ID", required = true) @PathVariable String id,
            @Valid @RequestBody UpdateRewardConfigRequest request) {
        return ApiResponse.success(profileCompletionRewardService.updateConfig(id, request));
    }

    @Operation(summary = "删除奖励配置", description = "删除信息采集奖励配置。角色: property_admin, platform_admin")
    @DeleteMapping("/admin/profile-rewards/{id}")
    public ApiResponse<Void> deleteConfig(
            @Parameter(description = "配置ID", required = true) @PathVariable String id) {
        profileCompletionRewardService.deleteConfig(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "触发信息采集奖励", description = "居民端触发指定字段的奖励。角色: resident")
    @PostMapping("/residents/me/profile-reward")
    public ApiResponse<TriggerRewardResponse> triggerReward(
            @Valid @RequestBody TriggerRewardRequest request) {
        String residentId = UserContext.currentUserId();
        return ApiResponse.success(profileCompletionRewardService.triggerReward(residentId, request.getFieldName()));
    }

    @Operation(summary = "我的奖励记录", description = "查询当前用户已获信息采集奖励记录。角色: resident")
    @GetMapping("/residents/me/profile-rewards")
    public ApiResponse<List<TriggerRewardResponse>> listMyRewards() {
        return ApiResponse.success(profileCompletionRewardService.listMyRewards());
    }

    @Operation(summary = "生成信息采集统计分享链接",
            description = "管理端生成信息采集统计 H5 分享链接/海报/二维码。角色: property_admin, platform_admin")
    @GetMapping("/admin/stats/share")
    public ApiResponse<StatsShareResponse> buildStatsShare(
            @Parameter(description = "物业公司ID(平台管理员可指定,物业管理员自动限定本物业)")
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(profileCompletionRewardService.buildStatsShare(propertyCompanyId));
    }
}
