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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端 — 商家审核", description = "商家积分购买审核、商家提现审核")
@RestController
@RequiredArgsConstructor
public class AdminMerchantAuditController {

    private final AdminMerchantAuditService adminMerchantAuditService;

    @Operation(summary = "商家积分购买审核列表", description = "获取商家积分购买审核列表。角色: property_admin, platform_admin")
    @GetMapping("/admin/merchant-point-purchases")
    public ApiResponse<PageResponse<MerchantPointPurchaseListItemResponse>> listPointPurchases(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "商家ID") @RequestParam(required = false) String merchantId,
            @Parameter(description = "审核状态") @RequestParam(required = false) String auditStatus,
            @Parameter(description = "商家状态") @RequestParam(required = false) String status,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminMerchantAuditService.listPointPurchases(
                page, pageSize, merchantId, auditStatus, status, startDate, endDate, propertyCompanyId));
    }

    @Operation(summary = "审核积分购买", description = "审核商家积分购买。角色: property_admin, platform_admin")
    @PostMapping("/admin/merchant-point-purchases/{id}/audit")
    public ApiResponse<AuditMerchantPointPurchaseResponse> auditPointPurchase(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Valid @RequestBody AuditMerchantPointPurchaseRequest request) {
        return ApiResponse.success(adminMerchantAuditService.auditPointPurchase(id, request));
    }

    @Operation(summary = "商家提现审核列表", description = "获取商家提现审核列表。角色: property_admin, platform_admin")
    @GetMapping("/admin/merchant-withdrawals")
    public ApiResponse<PageResponse<MerchantWithdrawalListItemResponse>> listWithdrawals(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "商家ID") @RequestParam(required = false) String merchantId,
            @Parameter(description = "审核状态") @RequestParam(required = false) String auditStatus,
            @Parameter(description = "商家状态") @RequestParam(required = false) String status,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminMerchantAuditService.listWithdrawals(
                page, pageSize, merchantId, auditStatus, status, startDate, endDate, propertyCompanyId));
    }

    @Operation(summary = "审核提现", description = "审核商家提现。角色: property_admin, platform_admin")
    @PostMapping("/admin/merchant-withdrawals/{id}/audit")
    public ApiResponse<AuditMerchantWithdrawalResponse> auditWithdrawal(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Valid @RequestBody AuditMerchantWithdrawalRequest request) {
        return ApiResponse.success(adminMerchantAuditService.auditWithdrawal(id, request));
    }
}
