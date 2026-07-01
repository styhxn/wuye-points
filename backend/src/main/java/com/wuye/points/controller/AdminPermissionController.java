package com.wuye.points.controller;

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
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限管理模块（管理端）
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPermissionController {

    private final AdminPermissionService adminPermissionService;

    @GetMapping("/permissions")
    public ApiResponse<PermissionListResponse> listPermissions() {
        return ApiResponse.success(adminPermissionService.listPermissions());
    }

    @GetMapping("/role-presets")
    public ApiResponse<RolePresetListResponse> listRolePresets(
            @RequestParam(required = false) String scope) {
        return ApiResponse.success(adminPermissionService.listRolePresets(scope));
    }

    @PostMapping("/role-presets")
    public ApiResponse<RolePresetItemResponse> createRolePreset(
            @Valid @RequestBody CreateRolePresetRequest request) {
        return ApiResponse.success(adminPermissionService.createRolePreset(request));
    }

    @PutMapping("/role-presets/{id}")
    public ApiResponse<RolePresetItemResponse> updateRolePreset(
            @PathVariable String id,
            @Valid @RequestBody UpdateRolePresetRequest request) {
        return ApiResponse.success(adminPermissionService.updateRolePreset(id, request));
    }

    @GetMapping("/users/{id}/permissions")
    public ApiResponse<UserPermissionsResponse> getUserPermissions(@PathVariable String id) {
        return ApiResponse.success(adminPermissionService.getUserPermissions(id));
    }

    @PostMapping("/users/{id}/permissions/grant")
    public ApiResponse<GrantPermissionsResponse> grantPermissions(
            @PathVariable String id,
            @Valid @RequestBody GrantPermissionsRequest request) {
        return ApiResponse.success(adminPermissionService.grantPermissions(id, request));
    }

    @PostMapping("/users/{id}/permissions/revoke")
    public ApiResponse<RevokePermissionsResponse> revokePermissions(
            @PathVariable String id,
            @Valid @RequestBody RevokePermissionsRequest request) {
        return ApiResponse.success(adminPermissionService.revokePermissions(id, request));
    }

    @GetMapping("/permission-change-logs")
    public ApiResponse<PageResponse<PermissionChangeLogItemResponse>> listPermissionChangeLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String residentId,
            @RequestParam(required = false) String operatorId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ApiResponse.success(adminPermissionService.listPermissionChangeLogs(
                page, pageSize, residentId, operatorId, action, startDate, endDate));
    }
}
