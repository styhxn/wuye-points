package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.platformconfig.PlatformConfigResponse;
import com.wuye.points.dto.platformconfig.UpdatePlatformConfigRequest;
import com.wuye.points.service.PlatformSettingService;
import com.wuye.points.util.RoleGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "平台配置模块", description = "归属开关、提现粒度、分成维度等平台级配置")
@RestController
@RequestMapping("/admin/platform-config")
@RequiredArgsConstructor
public class AdminPlatformConfigController {

    private final PlatformSettingService platformSettingService;
    private final RoleGuard roleGuard;

    @Operation(summary = "查看平台配置", description = "Roles: platform_admin, property_admin")
    @GetMapping
    public ApiResponse<PlatformConfigResponse> getConfig() {
        roleGuard.requireAnyRole("platform_admin", "property_admin");
        return ApiResponse.success(PlatformConfigResponse.builder()
                .rewardAttributionMode(platformSettingService.getRewardAttributionMode())
                .withdrawalGranularity(platformSettingService.getWithdrawalGranularity())
                .shareDimension(platformSettingService.getShareDimension())
                .build());
    }

    @Operation(summary = "更新平台配置", description = "仅更新非 null 字段。Roles: platform_admin")
    @PutMapping
    public ApiResponse<PlatformConfigResponse> updateConfig(
            @Valid @RequestBody UpdatePlatformConfigRequest request) {
        roleGuard.requireAnyRole("platform_admin");
        if (request.getRewardAttributionMode() != null) {
            platformSettingService.setRewardAttributionMode(request.getRewardAttributionMode());
        }
        if (request.getWithdrawalGranularity() != null) {
            platformSettingService.setWithdrawalGranularity(request.getWithdrawalGranularity());
        }
        if (request.getShareDimension() != null) {
            platformSettingService.setShareDimension(request.getShareDimension());
        }
        return getConfig();
    }
}
