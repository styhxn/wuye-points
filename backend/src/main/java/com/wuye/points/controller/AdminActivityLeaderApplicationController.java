package com.wuye.points.controller;

import com.wuye.points.dto.admin.activityleaderapplication.ActivityLeaderApplicationAuditResponse;
import com.wuye.points.dto.admin.activityleaderapplication.ActivityLeaderApplicationListItemResponse;
import com.wuye.points.dto.admin.activityleaderapplication.AuditActivityLeaderApplicationRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminActivityLeaderApplicationService;
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
 * 活动组组长申请审核（管理端）。
 * 审核通过时同事务创建组长身份（标签）并开通组长小店。
 */
@Tag(name = "管理端 — 活动组组长申请审核", description = "组长申请列表与审核；通过后创建组长身份并自动开通组长小店")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminActivityLeaderApplicationController {

    private final AdminActivityLeaderApplicationService adminActivityLeaderApplicationService;

    @Operation(summary = "组长申请列表",
            description = "用户提交的活动组组长申请列表（pending/approved/rejected）。角色: property_admin, platform_admin")
    @GetMapping("/activity-leader-applications")
    public ApiResponse<PageResponse<ActivityLeaderApplicationListItemResponse>> listApplications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String auditStatus,
            @Parameter(description = "物业公司ID（platform_admin 跨物业查询时传）")
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminActivityLeaderApplicationService.listApplications(
                page, pageSize, auditStatus, propertyCompanyId));
    }

    @Operation(summary = "审核组长申请",
            description = "approved 时同事务创建组长身份（标签，role 仍为 resident）并自动开通组长小店；"
                    + "rejected 时记录拒绝原因。角色: property_admin, platform_admin")
    @PostMapping("/activity-leader-applications/{applicationId}/audit")
    public ApiResponse<ActivityLeaderApplicationAuditResponse> auditApplication(
            @Parameter(description = "申请ID") @PathVariable String applicationId,
            @Valid @RequestBody AuditActivityLeaderApplicationRequest request) {
        return ApiResponse.success(adminActivityLeaderApplicationService.auditApplication(
                applicationId, request));
    }

    @Operation(summary = "撤销组长身份",
            description = "撤销某住户的活动组组长身份（标签）：身份记录置 revoked，历史业务数据保留。"
                    + "角色: property_admin, platform_admin")
    @PostMapping("/activity-leaders/{residentId}/revoke")
    public ApiResponse<ActivityLeaderApplicationAuditResponse> revokeIdentity(
            @Parameter(description = "住户ID") @PathVariable String residentId) {
        return ApiResponse.success(adminActivityLeaderApplicationService.revokeIdentity(residentId));
    }
}
