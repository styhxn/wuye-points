package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.payment.WalletCardService;
import com.wuye.points.payment.WalletCardsDTO;
import com.wuye.points.util.RoleGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 钱包「两张卡」视图接口（HTML ②-5）。
 *
 * <p>把同一主体的资金拆成平台内部钱包（卡 A）与 微信 分账冻结（卡 B）两类返回，供前端钱包页、
 * 商家/技工工作台统一展示。纯只读，不写库。</p>
 */
@Tag(name = "钱包两张卡", description = "统一资产视图：钱包余额 + 微信 分账冻结")
@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletCardController {

    private final WalletCardService walletCardService;
    private final RoleGuard roleGuard;

    /**
     * 查询某主体的两张卡。ownerType 取值见 {@code WechatSplitOwnerTypes}（RESIDENT/MERCHANT/COURIER/...）。
     *
     * <p>该接口最初用于内部资产视图，移动端无调用。由于可按任意 ownerId 查询他人钱包，
     * 现仅允许平台管理员使用；如需向住户/商家开放，必须改为从 token 推导主体。</p>
     */
    @Operation(summary = "查询两张卡",
            description = "ownerType 如 MERCHANT/COURIER/RESIDENT；ownerId 为主体 ID。角色: platform_admin")
    @GetMapping("/cards")
    public ApiResponse<WalletCardsDTO> getWalletCards(
            @Parameter(description = "主体类型") @RequestParam String ownerType,
            @Parameter(description = "主体 ID") @RequestParam String ownerId) {
        roleGuard.requireAnyRole("platform_admin");
        return ApiResponse.success(walletCardService.getWalletCards(ownerType, ownerId));
    }
}
