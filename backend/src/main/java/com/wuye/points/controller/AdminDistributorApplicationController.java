package com.wuye.points.controller;

import com.wuye.points.dto.admin.distributorapplication.AuditDistributorApplicationRequest;
import com.wuye.points.dto.admin.distributorapplication.DistributorApplicationAuditResponse;
import com.wuye.points.dto.admin.distributorapplication.DistributorApplicationListItemResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminDistributorApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 一级代理申请审核（管理端）。
 * 审核通过时由后端在同一事务内创建/关联商家载体并开通一级代理（is_distributor=1）。
 */
@Tag(name = "管理端 — 一级代理申请审核", description = "一级代理申请列表与审核；通过后自动创建商家载体并开通一级代理")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDistributorApplicationController {

    private final AdminDistributorApplicationService adminDistributorApplicationService;

    @Operation(summary = "一级代理申请列表",
            description = "用户提交的一级代理申请列表（pending/approved/rejected）。角色: property_admin, platform_admin")
    @GetMapping("/distributor-applications")
    public ApiResponse<PageResponse<DistributorApplicationListItemResponse>> listApplications(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "审核状态 pending/approved/rejected") @RequestParam(required = false) String auditStatus,
            @Parameter(description = "物业公司ID（platform_admin 跨物业查询时传）") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminDistributorApplicationService.listApplications(
                page, pageSize, auditStatus, propertyCompanyId));
    }

    @Operation(summary = "审核一级代理申请",
            description = "approved 时同事务创建/关联商家载体并开通一级代理（is_distributor=1、audit_status=approved）；"
                    + "rejected 时记录拒绝原因。角色: property_admin, platform_admin")
    @PostMapping("/distributor-applications/{applicationId}/audit")
    public ApiResponse<DistributorApplicationAuditResponse> auditApplication(
            @Parameter(description = "申请ID") @PathVariable String applicationId,
            @Valid @RequestBody AuditDistributorApplicationRequest request) {
        return ApiResponse.success(adminDistributorApplicationService.auditApplication(applicationId, request));
    }
}
