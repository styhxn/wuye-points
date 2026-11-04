package com.wuye.points.controller;

import com.wuye.points.dto.admin.merchant.MerchantWechatAccountRequest;
import com.wuye.points.dto.admin.merchant.MerchantWechatAccountResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.payment.MerchantWechatAccountAdminService;
import com.wuye.points.util.RoleGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 — 商家特约商户号（sub_mchid）绑定。
 *
 * <p>用于两种场景：① 进件审核通过后自动回填（本接口可查看/修正）；
 * ② 运营在微信服务商平台手动创建特约商户后，把 sub_mchid 补录到商家。
 * 未绑定 active 特约商户号的商家，商城订单只能使用积分/物业币支付（fail-closed）。</p>
 */
@Tag(name = "管理端 — 商家特约商户号", description = "商家 sub_mchid 的查询与绑定（服务商模式）")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminMerchantWechatAccountController {

    private final MerchantWechatAccountAdminService accountService;
    private final RoleGuard roleGuard;

    @Operation(summary = "查询商家特约商户号绑定", description = "角色: platform_admin")
    @GetMapping("/merchants/{merchantId}/wechat-account")
    public ApiResponse<MerchantWechatAccountResponse> get(@PathVariable String merchantId) {
        roleGuard.requireAnyRole("platform_admin");
        return ApiResponse.success(accountService.get(merchantId));
    }

    @Operation(summary = "绑定/修正商家特约商户号",
            description = "绑定后商城订单走服务商通道，资金直进商家特约商户号。角色: platform_admin")
    @PutMapping("/merchants/{merchantId}/wechat-account")
    public ApiResponse<MerchantWechatAccountResponse> bind(@PathVariable String merchantId,
                                                           @Valid @RequestBody MerchantWechatAccountRequest request) {
        roleGuard.requireAnyRole("platform_admin");
        return ApiResponse.success(accountService.bind(merchantId, request));
    }
}
