package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.merchant.settlement.MerchantCoinReceivableResponse;
import com.wuye.points.dto.merchant.withdrawal.ApplyWithdrawalRequest;
import com.wuye.points.dto.merchant.withdrawal.WithdrawalItemResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.service.MerchantCoinReceivableService;
import com.wuye.points.service.MerchantWithdrawalService;
import com.wuye.points.support.Idempotent;
import com.wuye.points.util.MerchantSupport;
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
 * 商家物业币应收（独立结算入口）
 */
@Tag(name = "商家物业币应收", description = "纯物业币/混合支付订单中，商家应得但商家特约商户号没有现金的部分")
@RestController
@RequestMapping("/merchant/coin-receivables")
@RequiredArgsConstructor
public class MerchantCoinReceivableController {

    private final MerchantSupport merchantSupport;
    private final MerchantCoinReceivableService merchantCoinReceivableService;
    private final MerchantWithdrawalService merchantWithdrawalService;

    @Operation(summary = "物业币应收明细", description = "按订单展示物业币应收及已结算/在途/可结算金额。Auth: Required | Roles: merchant")
    @GetMapping
    public ApiResponse<MerchantCoinReceivableResponse> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize) {
        Merchant merchant = merchantSupport.requireCurrentMerchant();
        return ApiResponse.success(merchantCoinReceivableService.list(merchant, page, pageSize));
    }

    @Operation(summary = "申请结算物业币应收",
            description = "从可结算余额发起内部结算申请，管理端审核完成后平台打款。Auth: Required | Roles: merchant；支持 X-Request-Id 幂等")
    @Idempotent
    @PostMapping("/withdrawals")
    public ApiResponse<WithdrawalItemResponse> apply(
            @Valid @RequestBody ApplyWithdrawalRequest request) {
        return ApiResponse.success(merchantWithdrawalService.applyCoinReceivable(request.getAmount()));
    }
}
