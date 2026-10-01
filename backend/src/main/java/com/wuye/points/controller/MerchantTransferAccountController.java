package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.transfer.BindTransferAccountRequest;
import com.wuye.points.dto.transfer.TransferAccountResponse;
import com.wuye.points.service.MerchantTransferAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 四层树角色收款账号（阶段2：内部结算出款前置）。
 *
 * <p>角色：property_admin(物业公司 PROPERTY) / coordinator(统筹) / sector_leader(板块) /
 * individual_leader(个体)。各角色绑定本人微信零钱 openid 或银行卡，作为商家转账收款方式
 * （前端「我的收款方式」页）。</p>
 */
@Tag(name = "收款账号（四层树内部结算出款）", description = "物业/统筹/板块/个体负责人绑定本人商家转账收款账号")
@RestController
@RequestMapping("/transfer-accounts")
@RequiredArgsConstructor
public class MerchantTransferAccountController {

    private final MerchantTransferAccountService accountService;

    @Operation(summary = "绑定我的收款账号",
            description = "zero=微信零钱(填openid)/bank=银行卡(填卡号、开户行、持卡人，后端加密落库)。同渠道重复绑定为覆盖更新。角色: property_admin, coordinator, sector_leader, individual_leader")
    @PostMapping
    public ApiResponse<TransferAccountResponse> bind(@Valid @RequestBody BindTransferAccountRequest request) {
        return ApiResponse.success(accountService.bind(request));
    }

    @Operation(summary = "查询我的收款账号列表",
            description = "返回已绑定的收款渠道（银行卡/持卡人信息不回显明文）。角色: property_admin, coordinator, sector_leader, individual_leader")
    @GetMapping("/my")
    public ApiResponse<List<TransferAccountResponse>> listMine() {
        return ApiResponse.success(accountService.listMine());
    }

    @Operation(summary = "解绑我的收款账号",
            description = "按渠道解绑（zero/bank）。角色: property_admin, coordinator, sector_leader, individual_leader")
    @DeleteMapping("/my")
    public ApiResponse<Void> unbind(
            @Parameter(description = "收款渠道：zero/bank") @RequestParam String channel) {
        accountService.unbind(channel);
        return ApiResponse.success(null);
    }
}
