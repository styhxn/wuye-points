package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.payment.PartnerApplymentProperties;
import com.wuye.points.payment.PartnerApplymentRules;
import com.wuye.points.payment.WechatDirectProperties;
import com.wuye.points.payment.WechatMerchantProperties;
import com.wuye.points.payment.WechatPayProperties;
import com.wuye.points.payment.WechatSplitProperties;
import com.wuye.points.service.MerchantApplymentService;
import com.wuye.points.util.RoleGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 管理端 — 微信支付双通道配置自检。
 *
 * <p>上线/联调排障用：只读返回两个商户通道的关键配置就绪情况（不返回任何密钥、私钥内容），
 * 便于在不看服务器日志的情况下确认「缺哪一项」。角色: platform_admin。</p>
 */
@Tag(name = "管理端 — 微信支付配置自检", description = "服务商/直连双通道配置就绪情况（不返回密钥）")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminWechatPayStatusController {

    private final WechatPayProperties partnerProps;
    private final WechatDirectProperties directProps;
    private final WechatSplitProperties splitProps;
    private final PartnerApplymentProperties applymentProps;
    private final MerchantApplymentService applymentService;
    private final RoleGuard roleGuard;

    @Operation(summary = "查询微信支付双通道配置状态", description = "角色: platform_admin")
    @GetMapping("/wechat-pay/status")
    public ApiResponse<Map<String, Object>> status() {
        roleGuard.requireAnyRole("platform_admin");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("partner", channel(partnerProps, partnerProps.getPlatformReceiverName()));
        result.put("direct", channel(directProps, null));
        Map<String, Object> split = new LinkedHashMap<>();
        split.put("configEnabled", splitProps.isEnabled());
        split.put("afterSaleDays", splitProps.getAfterSaleDays());
        split.put("maxSplitRatio", splitProps.getMaxSplitRatio());
        split.put("receiverRelationType", partnerProps.getReceiverRelationType());
        result.put("split", split);

        Map<String, Object> applyment = new LinkedHashMap<>();
        applyment.put("enabled", applymentProps.isEnabled());
        applyment.put("ready", applymentService.isApplymentReady());
        applyment.put("autoSubmit", applymentProps.isAutoSubmit());
        applyment.put("defaultIndustry",
                applymentProps.getQualificationType() == null || applymentProps.getQualificationType().isBlank()
                        ? PartnerApplymentRules.DEFAULT_INDUSTRY
                        : applymentProps.getQualificationType());
        applyment.put("settlementIdOverride", applymentProps.getSettlementId());
        applyment.put("settlementIdDefault", Map.of(
                "ENTERPRISE", PartnerApplymentRules.settlementIdFor("ENTERPRISE"),
                "INDIVIDUAL", PartnerApplymentRules.settlementIdFor("INDIVIDUAL")));
        result.put("applyment", applyment);
        return ApiResponse.success(result);
    }

    private Map<String, Object> channel(WechatPayProperties p, String platformReceiverName) {
        return channel((WechatMerchantProperties) p, platformReceiverName);
    }

    private Map<String, Object> channel(WechatDirectProperties p, String platformReceiverName) {
        return channel((WechatMerchantProperties) p, platformReceiverName);
    }

    /**
     * 只暴露「是否就绪」与脱敏配置，不返回 apiV3Key / 私钥。
     * 私钥文件路径仅报告文件是否存在，避免把服务器目录结构暴露给前端。
     */
    private Map<String, Object> channel(WechatMerchantProperties p,
                                        String platformReceiverName) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("enabled", p.isEnabled());
        m.put("configured", p.isConfigured());
        m.put("mchId", p.getMchId());
        m.put("appId", p.getAppId());
        m.put("notifyUrl", p.getNotifyUrl());
        m.put("notifyUrlConfigured", p.getNotifyUrl() != null && !p.getNotifyUrl().isBlank());
        m.put("verifyMode", p.isPublicKeyMode() ? "PUBLIC_KEY" : "PLATFORM_CERT");
        m.put("publicKeyId", p.getPublicKeyId());
        m.put("publicKeyPresent", publicKeyPresent(p));
        m.put("privateKeyPresent", privateKeyPresent(p));
        m.put("apiV3KeyLength", p.getApiV3Key() == null ? 0 : p.getApiV3Key().length());
        m.put("platformReceiverName", platformReceiverName);
        return m;
    }

    private boolean privateKeyPresent(WechatMerchantProperties p) {
        if (p.getPrivateKeyBase64() != null && !p.getPrivateKeyBase64().isBlank()) {
            return true;
        }
        return fileReadable(p.getPrivateKeyPath());
    }

    private boolean publicKeyPresent(WechatMerchantProperties p) {
        if (p.getPublicKeyBase64() != null && !p.getPublicKeyBase64().isBlank()) {
            return true;
        }
        return fileReadable(p.getPublicKeyPath());
    }

    private boolean fileReadable(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        try {
            return Files.isReadable(Path.of(path));
        } catch (Exception e) {
            return false;
        }
    }
}
