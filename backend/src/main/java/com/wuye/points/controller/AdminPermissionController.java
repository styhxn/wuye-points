package com.wuye.points.controller;

import com.wuye.points.dto.admin.permission.AccountListResponse;
import com.wuye.points.dto.admin.permission.CreateRolePresetRequest;
import com.wuye.points.dto.admin.permission.GrantPermissionsRequest;
import com.wuye.points.dto.admin.permission.GrantPermissionsResponse;
import com.wuye.points.dto.admin.permission.PermissionChangeLogItemResponse;
import com.wuye.points.dto.admin.permission.PermissionListResponse;
import com.wuye.points.dto.admin.permission.RevokePermissionsRequest;
import com.wuye.points.dto.admin.permission.RevokePermissionsResponse;
import com.wuye.points.dto.admin.permission.RolePresetItemResponse;
import com.wuye.points.dto.admin.permission.RolePresetListResponse;
import com.wuye.points.dto.admin.permission.UpdateRolePresetRequest;
import com.wuye.points.dto.admin.permission.UserPermissionsResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminPermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限管理模块（管理端）
 */
@Tag(name = "管理端 — 权限管理", description = "账号列表、权限管理、角色预设、权限变更日志")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPermissionController {

    private final AdminPermissionService adminPermissionService;

    @Operation(summary = "账号列表", description = "获取管理端账号列表。角色: property_admin, platform_admin")
    @GetMapping("/users")
    public ApiResponse<AccountListResponse> listAccounts(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminPermissionService.listAccounts(keyword));
    }

    @Operation(summary = "权限列表", description = "获取权限列表。角色: property_admin, platform_admin")
    @GetMapping("/permissions")
    public ApiResponse<PermissionListResponse> listPermissions() {
        return ApiResponse.success(adminPermissionService.listPermissions());
    }

    @Operation(summary = "角色预设列表", description = "获取角色预设列表。角色: property_admin, platform_admin")
    @GetMapping("/role-presets")
    public ApiResponse<RolePresetListResponse> listRolePresets(
            @Parameter(description = "权限范围") @RequestParam(required = false) String scope) {
        return ApiResponse.success(adminPermissionService.listRolePresets(scope));
    }

    @Operation(summary = "创建角色预设", description = "创建角色预设。角色: property_admin, platform_admin")
    @PostMapping("/role-presets")
    public ApiResponse<RolePresetItemResponse> createRolePreset(
            @Valid @RequestBody CreateRolePresetRequest request) {
        return ApiResponse.success(adminPermissionService.createRolePreset(request));
    }

    @Operation(summary = "更新角色预设", description = "更新角色预设信息。角色: property_admin, platform_admin")
    @PutMapping("/role-presets/{id}")
    public ApiResponse<RolePresetItemResponse> updateRolePreset(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Valid @RequestBody UpdateRolePresetRequest request) {
        return ApiResponse.success(adminPermissionService.updateRolePreset(id, request));
    }

    @Operation(summary = "获取用户权限", description = "获取指定用户的权限信息。角色: property_admin, platform_admin")
    @GetMapping("/users/{id}/permissions")
    public ApiResponse<UserPermissionsResponse> getUserPermissions(
            @Parameter(description = "资源ID") @PathVariable String id) {
        return ApiResponse.success(adminPermissionService.getUserPermissions(id));
    }

    @Operation(summary = "授予权限", description = "为用户授予权限。角色: property_admin, platform_admin")
    @PostMapping("/users/{id}/permissions/grant")
    public ApiResponse<GrantPermissionsResponse> grantPermissions(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Valid @RequestBody GrantPermissionsRequest request) {
        return ApiResponse.success(adminPermissionService.grantPermissions(id, request));
    }

    @Operation(summary = "撤销权限", description = "撤销用户权限。角色: property_admin, platform_admin")
    @PostMapping("/users/{id}/permissions/revoke")
    public ApiResponse<RevokePermissionsResponse> revokePermissions(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Valid @RequestBody RevokePermissionsRequest request) {
        return ApiResponse.success(adminPermissionService.revokePermissions(id, request));
    }

    @Operation(summary = "权限变更日志", description = "获取权限变更日志列表。角色: property_admin, platform_admin")
    @GetMapping("/permission-change-logs")
    public ApiResponse<PageResponse<PermissionChangeLogItemResponse>> listPermissionChangeLogs(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "用户ID") @RequestParam(required = false) String userId,
            @Parameter(description = "操作者ID") @RequestParam(required = false) String operatorId,
            @Parameter(description = "操作类型") @RequestParam(required = false) String action,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        return ApiResponse.success(adminPermissionService.listPermissionChangeLogs(
                page, pageSize, userId, operatorId, action, startDate, endDate));
    }
}
