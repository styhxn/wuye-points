package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.merchantad.CreateMerchantAdRequest;
import com.wuye.points.dto.merchantad.MerchantAdListItemResponse;
import com.wuye.points.dto.merchantad.MerchantAdQuoteResponse;
import com.wuye.points.dto.merchantad.MerchantAdQuotaResponse;
import com.wuye.points.dto.merchantad.TargetedMerchantAdRequest;
import com.wuye.points.service.MerchantAdService;
import com.wuye.points.support.Idempotent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商家广告", description = "商家广告额度与历史")
@RestController
@RequestMapping("/merchant/ads")
@RequiredArgsConstructor
public class MerchantAdController {

    private final MerchantAdService merchantAdService;

    @Operation(summary = "本周广告额度", description = "需登录，角色：merchant")
    @GetMapping("/quota")
    public ApiResponse<MerchantAdQuotaResponse> quota() {
        return ApiResponse.success(merchantAdService.getQuota());
    }

    @Operation(summary = "广告位按天计价试算", description = "需登录，角色：merchant；传展示天数返回应付金额与免费额度")
    @GetMapping("/quote")
    public ApiResponse<MerchantAdQuoteResponse> quote(
            @Parameter(description = "展示天数", example = "7") @RequestParam(defaultValue = "7") int days) {
        return ApiResponse.success(merchantAdService.quote(days));
    }

    @Operation(summary = "发布广告", description = "需登录，角色：merchant；广告位按天计价，仅支持 free（免费额度）/wechat（微信现金），"
            + "成功后 fan-out 到住户聊天列表。可携带 X-Request-Id 做幂等，避免重复提交生成多条广告")
    @Idempotent
    @PostMapping
    public ApiResponse<MerchantAdListItemResponse> create(
            @Valid @RequestBody CreateMerchantAdRequest request) {
        return ApiResponse.success(merchantAdService.createAd(request));
    }

    @Operation(summary = "创建定向广告", description = "需登录，角色：merchant；按天计价，支持按楼栋/性别/年龄段定向投放。"
            + "可携带 X-Request-Id 做幂等")
    @Idempotent
    @PostMapping("/targeted")
    public ApiResponse<MerchantAdListItemResponse> createTargetedAd(
            @Valid @RequestBody TargetedMerchantAdRequest request) {
        return ApiResponse.success(merchantAdService.createTargetedAd(request));
    }

    @Operation(summary = "待支付广告继续支付",
            description = "需登录，角色：merchant。为已存在且未支付的微信现金广告重新获取 payParams；"
                    + "已支付/已投放返回业务冲突，非微信现金广告返回 400。")
    @PostMapping("/{id}/repay")
    public ApiResponse<MerchantAdListItemResponse> repay(
            @Parameter(description = "广告ID") @PathVariable String id) {
        return ApiResponse.success(merchantAdService.repayAd(id));
    }

    @Operation(summary = "本店广告历史", description = "需登录，角色：merchant")
    @GetMapping
    public ApiResponse<PageResponse<MerchantAdListItemResponse>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(merchantAdService.listAds(page, pageSize));
    }
}
