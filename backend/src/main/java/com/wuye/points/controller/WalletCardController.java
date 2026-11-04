package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.rolewithdrawal.ApplyRoleWithdrawalRequest;
import com.wuye.points.dto.rolewithdrawal.RoleWithdrawalItemResponse;
import com.wuye.points.dto.wallet.ResidentWalletResponse;
import com.wuye.points.payment.WalletCardService;
import com.wuye.points.payment.WalletCardsDTO;
import com.wuye.points.service.RoleWithdrawalService;
import com.wuye.points.util.RoleGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 钱包接口：管理端「两张卡」资产视图 + 住户钱包余额/提现。
 */
@Tag(name = "钱包", description = "统一资产视图、住户钱包余额与提现")
@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletCardController {

    private final WalletCardService walletCardService;
    private final RoleWithdrawalService roleWithdrawalService;
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

    @Operation(summary = "我的钱包余额",
            description = "住户钱包：个人奖励金（物业币）余额、钱包累计余额、在途提现占用、可提现余额。角色: resident")
    @GetMapping("/my")
    public ApiResponse<ResidentWalletResponse> getMyWallet() {
        return ApiResponse.success(roleWithdrawalService.getMyWallet());
    }

    @Operation(summary = "住户钱包提现申请",
            description = "从奖励金转入的钱包余额申请提现；金额不得超过可提现余额。角色: resident")
    @PostMapping("/withdrawals")
    public ApiResponse<RoleWithdrawalItemResponse> applyWithdrawal(
            @Valid @RequestBody ApplyRoleWithdrawalRequest request) {
        return ApiResponse.success(roleWithdrawalService.applyForResidentWallet(request));
    }

    @Operation(summary = "我的钱包提现记录",
            description = "查询当前住户的钱包提现记录，可按审核状态/时间筛选。角色: resident")
    @GetMapping("/withdrawals")
    public ApiResponse<PageResponse<RoleWithdrawalItemResponse>> listWithdrawals(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "审核状态: pending/approved/rejected/completed")
            @RequestParam(required = false) String auditStatus,
            @Parameter(description = "开始日期 YYYY-MM-DD") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期 YYYY-MM-DD") @RequestParam(required = false) String endDate) {
        return ApiResponse.success(roleWithdrawalService.listForResidentWallet(
                page, pageSize, auditStatus, startDate, endDate));
    }
}
