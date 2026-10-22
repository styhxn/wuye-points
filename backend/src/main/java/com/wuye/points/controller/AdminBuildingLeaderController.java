package com.wuye.points.controller;

import com.wuye.points.dto.admin.buildingleader.AppointBuildingLeaderRequest;
import com.wuye.points.dto.admin.buildingleader.BuildingLeaderDetailResponse;
import com.wuye.points.dto.admin.buildingleader.BuildingLeaderListItemResponse;
import com.wuye.points.dto.admin.buildingleader.UpdateBuildingLeaderRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminBuildingLeaderService;
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

/**
 * 单元楼长管理模块（管理端）
 *
 * <p>物业公司可选定/添加/删除单元楼长角色。楼长与活动组无关，是与「小区 + 单元」绑定的推广角色。
 */
@Tag(name = "管理端 — 单元楼长管理", description = "单元楼长列表、指定、详情、更新、撤销")
@RestController
@RequestMapping("/admin/building-leaders")
@RequiredArgsConstructor
public class AdminBuildingLeaderController {

    private final AdminBuildingLeaderService adminBuildingLeaderService;

    @Operation(summary = "单元楼长列表", description = "获取单元楼长列表。角色: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<PageResponse<BuildingLeaderListItemResponse>> listBuildingLeaders(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "搜索关键词（住户姓名/手机号）") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态 active/inactive") @RequestParam(required = false) String status,
            @Parameter(description = "小区ID") @RequestParam(required = false) String communityId,
            @Parameter(description = "单元") @RequestParam(required = false) String unit,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminBuildingLeaderService.listBuildingLeaders(
                page, pageSize, keyword, status, communityId, unit, propertyCompanyId));
    }

    @Operation(summary = "指定单元楼长", description = "指定住户为某单元楼长；楼长为「住户 + 楼长」复合身份，住户角色不变。角色: property_admin, platform_admin")
    @PostMapping
    public ApiResponse<BuildingLeaderDetailResponse> appointBuildingLeader(
            @Valid @RequestBody AppointBuildingLeaderRequest request) {
        return ApiResponse.success(adminBuildingLeaderService.appointBuildingLeader(request));
    }

    @Operation(summary = "单元楼长详情", description = "获取单元楼长详情。角色: property_admin, platform_admin")
    @GetMapping("/{id}")
    public ApiResponse<BuildingLeaderDetailResponse> getBuildingLeader(
            @Parameter(description = "楼长ID bl_") @PathVariable String id) {
        return ApiResponse.success(adminBuildingLeaderService.getBuildingLeader(id));
    }

    @Operation(summary = "更新单元楼长", description = "更新楼长分成比例/状态/备注。角色: property_admin, platform_admin")
    @PutMapping("/{id}")
    public ApiResponse<BuildingLeaderDetailResponse> updateBuildingLeader(
            @Parameter(description = "楼长ID bl_") @PathVariable String id,
            @Valid @RequestBody UpdateBuildingLeaderRequest request) {
        return ApiResponse.success(adminBuildingLeaderService.updateBuildingLeader(id, request));
    }

    @Operation(summary = "撤销单元楼长", description = "撤销楼长（置为 inactive，无其他启用记录时回收角色）。角色: property_admin, platform_admin")
    @DeleteMapping("/{id}")
    public ApiResponse<BuildingLeaderDetailResponse> revokeBuildingLeader(
            @Parameter(description = "楼长ID bl_") @PathVariable String id,
            @Parameter(description = "撤销原因（可选）") @RequestParam(required = false) String reason) {
        return ApiResponse.success(adminBuildingLeaderService.revokeBuildingLeader(id, reason));
    }
}