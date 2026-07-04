package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.merchant.withdrawal.ApplyWithdrawalRequest;
import com.wuye.points.dto.merchant.withdrawal.WithdrawalItemResponse;
import com.wuye.points.service.MerchantWithdrawalService;
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
 * 商家提现
 */
@Tag(name = "商家提现模块", description = "商家提现申请、提现记录")
@RestController
@RequestMapping("/merchant/withdrawals")
@RequiredArgsConstructor
public class MerchantWithdrawalController {

    private final MerchantWithdrawalService merchantWithdrawalService;

    @Operation(summary = "申请商家提现", description = "商家提现申请。Auth: Required | Roles: merchant")
    @PostMapping
    public ApiResponse<WithdrawalItemResponse> apply(
            @Valid @RequestBody ApplyWithdrawalRequest request) {
        return ApiResponse.success(merchantWithdrawalService.apply(request));
    }

    @Operation(summary = "提现记录列表", description = "提现记录。Auth: Required | Roles: merchant")
    @GetMapping
    public ApiResponse<PageResponse<WithdrawalItemResponse>> listWithdrawals(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "审核状态") @RequestParam(required = false) String auditStatus,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        return ApiResponse.success(merchantWithdrawalService.listWithdrawals(
                page, pageSize, auditStatus, startDate, endDate));
    }
}
