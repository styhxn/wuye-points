package com.wuye.points.controller;

import com.wuye.points.dto.coinwithdrawal.ApplyCoinWithdrawalRequest;
import com.wuye.points.dto.coinwithdrawal.CoinWithdrawalItemResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.CoinWithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

/**
 * 物业币兑换申请
 */
@Tag(name = "物业币兑换模块", description = "物业币兑换申请、兑换记录")
@RestController
@RequestMapping("/coin-withdrawals")
@RequiredArgsConstructor
public class CoinWithdrawalController {

    private final CoinWithdrawalService coinWithdrawalService;

    @Operation(summary = "申请物业币兑换", description = "物业币兑换申请。Auth: Required | Roles: merchant")
    @PostMapping
    public ApiResponse<CoinWithdrawalItemResponse> apply(
            @Valid @RequestBody ApplyCoinWithdrawalRequest request) {
        return ApiResponse.success(coinWithdrawalService.apply(request));
    }

    @Operation(summary = "兑换记录列表", description = "兑换记录。Auth: Required | Roles: merchant, platform_admin, property_admin")
    @GetMapping
    public ApiResponse<PageResponse<CoinWithdrawalItemResponse>> listWithdrawals(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "商家ID") @RequestParam(required = false) String merchantId,
            @Parameter(description = "审核状态") @RequestParam(required = false) String auditStatus,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        return ApiResponse.success(coinWithdrawalService.listWithdrawals(
                page, pageSize, merchantId, auditStatus, startDate, endDate));
    }
}
