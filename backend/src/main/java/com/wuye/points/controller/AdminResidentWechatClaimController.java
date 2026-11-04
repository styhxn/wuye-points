package com.wuye.points.controller;

import com.wuye.points.dto.admin.resident.ClaimWechatIdentityRequest;
import com.wuye.points.dto.admin.resident.ClaimWechatIdentityResponse;
import com.wuye.points.dto.admin.resident.WechatClaimSourceResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.service.WechatIdentityClaimService;
import com.wuye.points.util.RoleGuard;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端 — 住户微信身份认领。
 *
 * <p>解决「管理端预建账号没有 openid、本人微信登录又新建了一条记录」导致的分账个人接收方登记不到人：
 * 运营在目标账号上列出同手机号的微信身份记录，一键把 openid 迁过来。只迁微信身份，不迁订单/钱包数据。</p>
 */
@Tag(name = "管理端 — 住户微信身份认领", description = "把小程序登录产生的 openid 迁到管理端预建账号")
@RestController
@RequestMapping("/admin/residents")
@RequiredArgsConstructor
public class AdminResidentWechatClaimController {

    private final WechatIdentityClaimService claimService;
    private final RoleGuard roleGuard;

    @Operation(summary = "列出可认领的微信身份记录",
            description = "按目标账号的手机号，找出同手机号且带 openid 的其他住户记录。"
                    + "openid 只回脱敏值。角色: property_admin, platform_admin")
    @GetMapping("/{id}/wechat-claim-sources")
    public ApiResponse<List<WechatClaimSourceResponse>> listClaimSources(
            @Parameter(description = "目标账号ID（residents.id）") @PathVariable String id) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        return ApiResponse.success(claimService.listClaimSources(id));
    }

    @Operation(summary = "认领微信身份",
            description = "把来源记录的 openid/unionid 迁到目标账号，来源记录随后失去微信登录入口。"
                    + "要求：目标无 openid、两者手机号一致、来源无积分/物业币残留。"
                    + "认领后请通知本人在小程序重新登录一次。角色: property_admin, platform_admin")
    @PostMapping("/{id}/wechat-claim")
    public ApiResponse<ClaimWechatIdentityResponse> claim(
            @Parameter(description = "目标账号ID（residents.id）") @PathVariable String id,
            @Valid @RequestBody ClaimWechatIdentityRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        return ApiResponse.success(claimService.claim(id, request));
    }
}
