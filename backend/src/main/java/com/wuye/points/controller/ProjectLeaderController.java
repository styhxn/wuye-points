package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.projectleader.CreateProjectLeaderRequest;
import com.wuye.points.dto.projectleader.ProjectLeaderDetailResponse;
import com.wuye.points.dto.projectleader.ProjectLeaderListItemResponse;
import com.wuye.points.dto.projectleader.UpdateProjectLeaderRequest;
import com.wuye.points.service.ProjectLeaderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 项目负责人模块（B1）
 */
@Tag(name = "项目负责人", description = "项目负责人列表、详情、创建、更新、删除")
@RestController
@RequestMapping("/project-leaders")
@RequiredArgsConstructor
public class ProjectLeaderController {

    private final ProjectLeaderService projectLeaderService;

    @Operation(summary = "项目负责人列表",
            description = "查询项目负责人列表。角色: property_admin, platform_admin, coordinator, sector_leader, resident")
    @GetMapping
    public ApiResponse<List<ProjectLeaderListItemResponse>> listProjectLeaders(
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "区域负责人ID") @RequestParam(required = false) String regionalLeaderId) {
        return ApiResponse.success(projectLeaderService.listProjectLeaders(propertyCompanyId, regionalLeaderId));
    }

    @Operation(summary = "我的项目负责人信息",
            description = "获取当前用户作为项目负责人的信息。Auth: Required")
    @GetMapping("/my")
    public ApiResponse<ProjectLeaderDetailResponse> getMyProjectLeader() {
        return ApiResponse.success(projectLeaderService.getMyProjectLeader());
    }

    @Operation(summary = "创建项目负责人",
            description = "创建项目负责人。角色: coordinator, property_admin, sector_leader")
    @PostMapping
    public ApiResponse<ProjectLeaderDetailResponse> createProjectLeader(
            @Valid @RequestBody CreateProjectLeaderRequest request) {
        return ApiResponse.success(projectLeaderService.createProjectLeader(request));
    }

    @Operation(summary = "项目负责人详情",
            description = "获取项目负责人详情。角色: property_admin, platform_admin, coordinator, sector_leader")
    @GetMapping("/{id}")
    public ApiResponse<ProjectLeaderDetailResponse> getProjectLeader(
            @Parameter(description = "项目负责人ID") @PathVariable String id) {
        return ApiResponse.success(projectLeaderService.getProjectLeader(id));
    }

    @Operation(summary = "更新项目负责人",
            description = "更新项目负责人信息。角色: coordinator, property_admin, sector_leader")
    @PutMapping("/{id}")
    public ApiResponse<ProjectLeaderDetailResponse> updateProjectLeader(
            @Parameter(description = "项目负责人ID") @PathVariable String id,
            @Valid @RequestBody UpdateProjectLeaderRequest request) {
        return ApiResponse.success(projectLeaderService.updateProjectLeader(id, request));
    }

    @Operation(summary = "删除项目负责人",
            description = "删除项目负责人（软删除，置为 inactive）。角色: coordinator, property_admin, sector_leader")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProjectLeader(
            @Parameter(description = "项目负责人ID") @PathVariable String id) {
        projectLeaderService.deleteProjectLeader(id);
        return ApiResponse.success(null);
    }
}
