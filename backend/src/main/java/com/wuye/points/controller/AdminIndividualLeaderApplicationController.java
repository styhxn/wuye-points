package com.wuye.points.controller;

import com.wuye.points.dto.admin.individualleader.AuditIndividualLeaderApplicationRequest;
import com.wuye.points.dto.admin.individualleader.IndividualLeaderApplicationAuditResponse;
import com.wuye.points.dto.admin.individualleader.IndividualLeaderApplicationListItemResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminIndividualLeaderService;
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
 * 个体负责人申请审核（管理端）。
 *
 * <p>2026-09-08 前端问题：GET /admin/individual-leaders/applications 被 AdminIndividualLeaderController 的
 * {@code GET /admin/individual-leaders/{id}} 抢先匹配，导致 400「ID格式不正确」。
 * 这里用独立的 Controller 注册字面量子路径 /applications，Spring 的字面量优先级高于路径变量，
 * 因此天然优先于 /{id}；同时保留一条兼容别名 /admin/individual-leader-applications。
 */
@Tag(name = "管理端 — 个体负责人申请审核", description = "一级代理申请列表与审核")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminIndividualLeaderApplicationController {

    private final AdminIndividualLeaderService adminIndividualLeaderService;

    @Operation(summary = "个体负责人申请列表",
            description = "住户提交的一级代理申请列表（pending/approved/rejected）。角色: property_admin, platform_admin")
    @GetMapping("/individual-leaders/applications")
    public ApiResponse<PageResponse<IndividualLeaderApplicationListItemResponse>> listApplications(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "审核状态") @RequestParam(required = false) String auditStatus,
            @Parameter(description = "申请板块") @RequestParam(required = false) String sector,
            @Parameter(description = "搜索关键词（姓名/手机号）") @RequestParam(required = false) String keyword,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminIndividualLeaderService.listApplications(
                page, pageSize, auditStatus, sector, keyword, propertyCompanyId));
    }

    @Operation(summary = "审核个体负责人申请",
            description = "approved 时创建个体负责人并变更住户角色；rejected 时记录拒绝原因。角色: property_admin, platform_admin")
    @PostMapping("/individual-leaders/applications/{applicationId}/audit")
    public ApiResponse<IndividualLeaderApplicationAuditResponse> auditApplication(
            @Parameter(description = "申请ID") @PathVariable String applicationId,
            @Valid @RequestBody AuditIndividualLeaderApplicationRequest request) {
        return ApiResponse.success(adminIndividualLeaderService.auditApplication(applicationId, request));
    }

    @Operation(summary = "个体负责人申请列表（兼容别名，已废弃）",
            description = "与 GET /admin/individual-leaders/applications 等价，仅为兼容旧前端路径保留")
    @GetMapping("/individual-leader-applications")
    public ApiResponse<PageResponse<IndividualLeaderApplicationListItemResponse>> listApplicationsAlias(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "审核状态") @RequestParam(required = false) String auditStatus,
            @Parameter(description = "申请板块") @RequestParam(required = false) String sector,
            @Parameter(description = "搜索关键词（姓名/手机号）") @RequestParam(required = false) String keyword,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminIndividualLeaderService.listApplications(
                page, pageSize, auditStatus, sector, keyword, propertyCompanyId));
    }

    @Operation(summary = "审核个体负责人申请（兼容别名，已废弃）",
            description = "与 POST /admin/individual-leaders/applications/{applicationId}/audit 等价")
    @PostMapping("/individual-leader-applications/{applicationId}/audit")
    public ApiResponse<IndividualLeaderApplicationAuditResponse> auditApplicationAlias(
            @Parameter(description = "申请ID") @PathVariable String applicationId,
            @Valid @RequestBody AuditIndividualLeaderApplicationRequest request) {
        return ApiResponse.success(adminIndividualLeaderService.auditApplication(applicationId, request));
    }
}
