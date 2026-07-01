package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.merchant.withdrawal.ApplyWithdrawalRequest;
import com.wuye.points.dto.merchant.withdrawal.WithdrawalItemResponse;
import com.wuye.points.service.MerchantWithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家提现
 */
@RestController
@RequestMapping("/merchant/withdrawals")
@RequiredArgsConstructor
public class MerchantWithdrawalController {

    private final MerchantWithdrawalService merchantWithdrawalService;

    /**
     * 商家提现申请
     * Auth: Required | Roles: merchant
     */
    @PostMapping
    public ApiResponse<WithdrawalItemResponse> apply(
            @Valid @RequestBody ApplyWithdrawalRequest request) {
        return ApiResponse.success(merchantWithdrawalService.apply(request));
    }

    /**
     * 提现记录
     * Auth: Required | Roles: merchant
     */
    @GetMapping
    public ApiResponse<PageResponse<WithdrawalItemResponse>> listWithdrawals(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String auditStatus,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ApiResponse.success(merchantWithdrawalService.listWithdrawals(
                page, pageSize, auditStatus, startDate, endDate));
    }
}
