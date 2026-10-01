package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.companyaccount.CompanyAccountBalanceResponse;
import com.wuye.points.dto.companyaccount.CompanyAccountRecordItemResponse;
import com.wuye.points.service.CompanyAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 公司账户资金流模块（A10）
 */
@Tag(name = "公司账户资金流", description = "领导账户资金流水查询、手动调整")
@RestController
@RequestMapping("/admin/company-account")
@RequiredArgsConstructor
public class CompanyAccountController {

    private final CompanyAccountService companyAccountService;

    @Operation(summary = "公司账户余额",
            description = "查询物业公司账户余额。角色: property_admin, platform_admin（property_admin 仅可查本公司）")
    @GetMapping("/balance")
    public ApiResponse<CompanyAccountBalanceResponse> getBalance(
            @Parameter(description = "物业公司ID（property_admin 自动限定本公司）")
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(companyAccountService.getBalance(propertyCompanyId));
    }

    @Operation(summary = "公司账户流水",
            description = "查询物业公司账户流水（分页）。角色: property_admin, platform_admin")
    @GetMapping("/records")
    public ApiResponse<PageResponse<CompanyAccountRecordItemResponse>> listRecords(
            @Parameter(description = "物业公司ID（property_admin 自动限定本公司）")
            @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(companyAccountService.listRecords(propertyCompanyId, page, pageSize));
    }

    @Operation(summary = "手动调整公司账户",
            description = "平台管理员手动调整公司账户余额。角色: platform_admin")
    @PostMapping("/adjust")
    public ApiResponse<CompanyAccountRecordItemResponse> adminAdjust(
            @Valid @RequestBody AdminAdjustRequest request) {
        return ApiResponse.success(companyAccountService.adminAdjust(
                request.getPropertyCompanyId(), request.getAmount(), request.getRemark()));
    }

    @Data
    public static class AdminAdjustRequest {
        @NotBlank(message = "propertyCompanyId不能为空")
        private String propertyCompanyId;

        @NotNull(message = "amount不能为空")
        private BigDecimal amount;

        private String remark;
    }
}
