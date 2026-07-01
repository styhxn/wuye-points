package com.wuye.points.controller;

import com.wuye.points.dto.admin.coinwithdrawal.AuditCoinWithdrawalRequest;
import com.wuye.points.dto.admin.coinwithdrawal.AuditCoinWithdrawalResponse;
import com.wuye.points.dto.admin.coinwithdrawal.CoinWithdrawalListItemResponse;
import com.wuye.points.dto.admin.merchantaudit.AuditMerchantPointPurchaseRequest;
import com.wuye.points.dto.admin.merchantaudit.AuditMerchantPointPurchaseResponse;
import com.wuye.points.dto.admin.merchantaudit.AuditMerchantWithdrawalRequest;
import com.wuye.points.dto.admin.merchantaudit.AuditMerchantWithdrawalResponse;
import com.wuye.points.dto.admin.merchantaudit.MerchantPointPurchaseListItemResponse;
import com.wuye.points.dto.admin.merchantaudit.MerchantWithdrawalListItemResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminMerchantAuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminMerchantAuditController {

    private final AdminMerchantAuditService adminMerchantAuditService;

    @GetMapping("/admin/merchant-point-purchases")
    public ApiResponse<PageResponse<MerchantPointPurchaseListItemResponse>> listPointPurchases(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) String auditStatus,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminMerchantAuditService.listPointPurchases(
                page, pageSize, merchantId, auditStatus, status, startDate, endDate, propertyCompanyId));
    }

    @PostMapping("/admin/merchant-point-purchases/{id}/audit")
    public ApiResponse<AuditMerchantPointPurchaseResponse> auditPointPurchase(
            @PathVariable String id,
            @Valid @RequestBody AuditMerchantPointPurchaseRequest request) {
        return ApiResponse.success(adminMerchantAuditService.auditPointPurchase(id, request));
    }

    @GetMapping("/admin/merchant-withdrawals")
    public ApiResponse<PageResponse<MerchantWithdrawalListItemResponse>> listWithdrawals(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) String auditStatus,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminMerchantAuditService.listWithdrawals(
                page, pageSize, merchantId, auditStatus, status, startDate, endDate, propertyCompanyId));
    }

    @PostMapping("/admin/merchant-withdrawals/{id}/audit")
    public ApiResponse<AuditMerchantWithdrawalResponse> auditWithdrawal(
            @PathVariable String id,
            @Valid @RequestBody AuditMerchantWithdrawalRequest request) {
        return ApiResponse.success(adminMerchantAuditService.auditWithdrawal(id, request));
    }
}
