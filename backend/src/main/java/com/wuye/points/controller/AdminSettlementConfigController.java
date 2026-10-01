package com.wuye.points.controller;

import com.wuye.points.dto.admin.settlement.SettlementConfigResponse;
import com.wuye.points.dto.admin.settlement.UpdateSettlementConfigRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.service.PlatformSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 — 结算/分账模式开关
 * <p>集中管理：真分账(微信)模式、记账兜底模式、自动提现、平台提现手续费（总开关 + 费率 + 分成比例 + 按类别开关）。
 * 配置通过 {@link PlatformSettingService} 持久化到平台级 JSON 文件，所有提现/分账服务实时读取。
 */
@Tag(name = "管理端 — 结算/分账模式开关", description = "结算与分账模式开关、平台提现手续费配置（费率可自由调整）")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminSettlementConfigController {

    private final PlatformSettingService platformSettingService;

    @Operation(summary = "查询结算/分账模式配置",
            description = "返回当前所有结算模式开关与平台提现手续费配置。角色: platform_admin")
    @GetMapping("/settlement-config")
    public ApiResponse<SettlementConfigResponse> getConfig() {
        SettlementConfigResponse response = new SettlementConfigResponse();
        response.setWechatSplitEnabled(platformSettingService.isWechatSplitEnabled());
        response.setLedgerFallbackEnabled(platformSettingService.isLedgerFallbackEnabled());
        response.setAutoWithdrawalEnabled(platformSettingService.isAutoWithdrawalEnabled());
        response.setPlatformWithdrawalFeeEnabled(platformSettingService.isPlatformWithdrawalFeeEnabled());
        response.setPlatformWithdrawalFeeRate(platformSettingService.getPlatformWithdrawalFeeRate());
        response.setPlatformWithdrawalFeeShareRate(platformSettingService.getPlatformWithdrawalFeeShareRate());
        response.setFeeForMerchant(platformSettingService.isFeeForMerchant());
        response.setFeeForRole(platformSettingService.isFeeForRole());
        response.setFeeForProperty(platformSettingService.isFeeForProperty());
        response.setFeeForCoin(platformSettingService.isFeeForCoin());
        return ApiResponse.success(response);
    }

    @Operation(summary = "更新结算/分账模式配置",
            description = "仅更新请求中非 null 的字段。角色: platform_admin。"
                    + "约束：真分账(微信)与记账兜底至少需启用一种；费率与分成比例需在 0~1 之间。")
    @PutMapping("/settlement-config")
    public ApiResponse<SettlementConfigResponse> updateConfig(
            @Valid @RequestBody UpdateSettlementConfigRequest request) {
        platformSettingService.updateSettlementConfig(new PlatformSettingService.SettlementConfigUpdate(
                request.getWechatSplitEnabled(),
                request.getLedgerFallbackEnabled(),
                request.getAutoWithdrawalEnabled(),
                request.getPlatformWithdrawalFeeEnabled(),
                request.getPlatformWithdrawalFeeRate(),
                request.getPlatformWithdrawalFeeShareRate(),
                request.getFeeForMerchant(),
                request.getFeeForRole(),
                request.getFeeForProperty(),
                request.getFeeForCoin()));
        return getConfig();
    }
}
