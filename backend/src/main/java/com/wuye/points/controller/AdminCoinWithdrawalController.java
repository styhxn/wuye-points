package com.wuye.points.controller;

import com.wuye.points.dto.admin.coinwithdrawal.AuditCoinWithdrawalRequest;
import com.wuye.points.dto.admin.coinwithdrawal.AuditCoinWithdrawalResponse;
import com.wuye.points.dto.admin.coinwithdrawal.CoinWithdrawalListItemResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminCoinWithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端 — 物业币提现审核", description = "物业币提现审核列表、审核")
@RestController
@RequestMapping("/admin/coin-withdrawals")
@RequiredArgsConstructor
public class AdminCoinWithdrawalController {

    private final AdminCoinWithdrawalService adminCoinWithdrawalService;

    @Operation(summary = "物业币提现审核列表", description = "获取物业币提现审核列表。角色: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<PageResponse<CoinWithdrawalListItemResponse>> listWithdrawals(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "商家ID") @RequestParam(required = false) String merchantId,
            @Parameter(description = "审核状态") @RequestParam(required = false) String auditStatus,
            @Parameter(description = "提现状态") @RequestParam(required = false) String status,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminCoinWithdrawalService.listWithdrawals(
                page, pageSize, merchantId, auditStatus, status, startDate, endDate, propertyCompanyId));
    }

    @Operation(summary = "审核物业币提现", description = "审核物业币提现申请。角色: property_admin, platform_admin")
    @PostMapping("/{id}/audit")
    public ApiResponse<AuditCoinWithdrawalResponse> audit(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Valid @RequestBody AuditCoinWithdrawalRequest request) {
        return ApiResponse.success(adminCoinWithdrawalService.audit(id, request));
    }
}
