package com.wuye.points.controller;

import com.wuye.points.dto.coinfund.PropertyCoinFundLedgerItemResponse;
import com.wuye.points.dto.coinfund.PropertyCoinFundSummaryResponse;
import com.wuye.points.dto.coinfund.TransferPropertyCoinFundRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.PropertyCoinFundService;
import com.wuye.points.util.PermissionGuard;
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

import java.util.List;

/**
 * 物业币现金资金归属（管理端）
 */
@Tag(name = "物业币资金归属", description = "充值现金按物业公司归属、商家物业币结算台账、平台转物业")
@RestController
@RequestMapping("/admin/property-coin-funds")
@RequiredArgsConstructor
public class AdminPropertyCoinFundController {

    private final PropertyCoinFundService propertyCoinFundService;
    private final PermissionGuard permissionGuard;

    @Operation(summary = "资金账户汇总", description = "按物业公司查看代管资金余额、累计入账/出账。Auth: Required | Roles: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<List<PropertyCoinFundSummaryResponse>> listSummary(
            @Parameter(description = "物业公司ID（平台端可选，物业端固定本物业）")
            @RequestParam(required = false) String propertyCompanyId) {
        permissionGuard.requireReadPermissionOrAdmin("pool.read");
        return ApiResponse.success(propertyCoinFundService.listSummary(propertyCompanyId));
    }

    @Operation(summary = "资金台账", description = "充值入账/商家物业币结算/平台转物业明细。Auth: Required | Roles: property_admin, platform_admin")
    @GetMapping("/records")
    public ApiResponse<PageResponse<PropertyCoinFundLedgerItemResponse>> listLedger(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "记账类型") @RequestParam(required = false) String entryType) {
        permissionGuard.requireReadPermissionOrAdmin("pool.read");
        return ApiResponse.success(propertyCoinFundService.listLedger(
                page, pageSize, propertyCompanyId, entryType));
    }

    @Operation(summary = "平台向物业划转代管资金", description = "从物业币代管资金余额出账并登记转账凭证。Auth: Required | Roles: platform_admin")
    @PostMapping("/transfers")
    public ApiResponse<PropertyCoinFundLedgerItemResponse> transferToProperty(
            @Valid @RequestBody TransferPropertyCoinFundRequest request) {
        permissionGuard.requirePermission("pool.adjust");
        return ApiResponse.success(propertyCoinFundService.transferToProperty(request));
    }
}
