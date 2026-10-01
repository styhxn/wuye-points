package com.wuye.points.controller;

import com.wuye.points.dto.admin.rolewithdrawal.AdminRoleWithdrawalListItemResponse;
import com.wuye.points.dto.admin.rolewithdrawal.AdminRoleWithdrawalSummaryResponse;
import com.wuye.points.dto.admin.rolewithdrawal.AuditRoleWithdrawalRequest;
import com.wuye.points.dto.admin.rolewithdrawal.AuditRoleWithdrawalResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminRoleWithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端 — 配送员/多角色提现审批", description = "配送员/个体负责人/统筹/板块负责人提现查询与审核")
@RestController
@RequiredArgsConstructor
public class AdminRoleWithdrawalController {

    private final AdminRoleWithdrawalService adminRoleWithdrawalService;

    @Operation(summary = "配送员/多角色提现审批汇总（看板）",
            description = "配送员/个体负责人/统筹/板块负责人提现审批看板汇总：待审笔数/金额、今日已审、已通过合计。角色: property_admin, platform_admin")
    @GetMapping("/admin/role-withdrawals/summary")
    public ApiResponse<AdminRoleWithdrawalSummaryResponse> getSummary(
            @Parameter(description = "物业公司ID（platform_admin可指定，否则取当前）")
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminRoleWithdrawalService.getSummary(propertyCompanyId));
    }

    @Operation(summary = "配送员/多角色提现审核列表",
            description = "查询配送员/个体负责人/统筹/板块负责人提现记录，可按提现类型/审核状态/关键词/时间筛选。角色: property_admin, platform_admin")
    @GetMapping("/admin/role-withdrawals")
    public ApiResponse<PageResponse<AdminRoleWithdrawalListItemResponse>> listWithdrawals(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "提现类型: individual_leader/courier/coordinator/sector_leader")
            @RequestParam(required = false) String withdrawalType,
            @Parameter(description = "审核状态: pending/approved/rejected/completed")
            @RequestParam(required = false) String auditStatus,
            @Parameter(description = "关键词（提现ID/申请人ID/住户姓名/手机号）")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminRoleWithdrawalService.listWithdrawals(
                page, pageSize, withdrawalType, auditStatus, keyword, startDate, endDate, propertyCompanyId));
    }

    @Operation(summary = "审核配送员/多角色提现",
            description = "审核配送员/个体负责人/统筹/板块负责人提现：pending→approved/rejected；approved→completed（打款完成）/rejected。角色: property_admin, platform_admin")
    @PostMapping("/admin/role-withdrawals/{id}/audit")
    public ApiResponse<AuditRoleWithdrawalResponse> audit(
            @Parameter(description = "提现记录ID") @PathVariable String id,
            @Valid @RequestBody AuditRoleWithdrawalRequest request) {
        return ApiResponse.success(adminRoleWithdrawalService.audit(id, request));
    }
}
