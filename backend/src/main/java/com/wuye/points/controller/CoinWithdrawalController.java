package com.wuye.points.controller;

import com.wuye.points.dto.coinwithdrawal.ApplyCoinWithdrawalRequest;
import com.wuye.points.dto.coinwithdrawal.CoinWithdrawalItemResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.CoinWithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 物业币兑换申请
 */
@RestController
@RequestMapping("/coin-withdrawals")
@RequiredArgsConstructor
public class CoinWithdrawalController {

    private final CoinWithdrawalService coinWithdrawalService;

    /**
     * 物业币兑换申请
     * Auth: Required | Roles: merchant
     */
    @PostMapping
    public ApiResponse<CoinWithdrawalItemResponse> apply(
            @Valid @RequestBody ApplyCoinWithdrawalRequest request) {
        return ApiResponse.success(coinWithdrawalService.apply(request));
    }

    /**
     * 兑换记录
     * Auth: Required | Roles: merchant, platform_admin, property_admin
     */
    @GetMapping
    public ApiResponse<PageResponse<CoinWithdrawalItemResponse>> listWithdrawals(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) String auditStatus,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ApiResponse.success(coinWithdrawalService.listWithdrawals(
                page, pageSize, merchantId, auditStatus, startDate, endDate));
    }
}
