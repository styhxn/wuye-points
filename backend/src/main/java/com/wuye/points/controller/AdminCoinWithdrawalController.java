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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/coin-withdrawals")
@RequiredArgsConstructor
public class AdminCoinWithdrawalController {

    private final AdminCoinWithdrawalService adminCoinWithdrawalService;

    @GetMapping
    public ApiResponse<PageResponse<CoinWithdrawalListItemResponse>> listWithdrawals(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) String auditStatus,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminCoinWithdrawalService.listWithdrawals(
                page, pageSize, merchantId, auditStatus, status, startDate, endDate, propertyCompanyId));
    }

    @PostMapping("/{id}/audit")
    public ApiResponse<AuditCoinWithdrawalResponse> audit(
            @PathVariable String id,
            @Valid @RequestBody AuditCoinWithdrawalRequest request) {
        return ApiResponse.success(adminCoinWithdrawalService.audit(id, request));
    }
}
