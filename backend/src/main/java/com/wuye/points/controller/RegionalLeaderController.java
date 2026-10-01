package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.regionallistleader.CreateRegionalLeaderRequest;
import com.wuye.points.dto.regionallistleader.RegionalLeaderDetailResponse;
import com.wuye.points.dto.regionallistleader.RegionalLeaderListItemResponse;
import com.wuye.points.dto.regionallistleader.UpdateRegionalLeaderRequest;
import com.wuye.points.service.RegionalLeaderService;
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
 * 区域负责人模块（B1）
 */
@Tag(name = "区域负责人", description = "区域负责人列表、详情、创建、更新、删除")
@RestController
@RequestMapping("/regional-leaders")
@RequiredArgsConstructor
public class RegionalLeaderController {

    private final RegionalLeaderService regionalLeaderService;

    @Operation(summary = "区域负责人列表",
            description = "查询区域负责人列表。角色: property_admin, platform_admin, coordinator, sector_leader")
    @GetMapping
    public ApiResponse<List<RegionalLeaderListItemResponse>> listRegionalLeaders(
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "板块负责人ID") @RequestParam(required = false) String sectorLeaderId) {
        return ApiResponse.success(regionalLeaderService.listRegionalLeaders(propertyCompanyId, sectorLeaderId));
    }

    @Operation(summary = "我的区域负责人信息",
            description = "获取当前用户作为区域负责人的信息。Auth: Required")
    @GetMapping("/my")
    public ApiResponse<RegionalLeaderDetailResponse> getMyRegionalLeader() {
        return ApiResponse.success(regionalLeaderService.getMyRegionalLeader());
    }

    @Operation(summary = "创建区域负责人",
            description = "创建区域负责人。角色: coordinator, property_admin, sector_leader")
    @PostMapping
    public ApiResponse<RegionalLeaderDetailResponse> createRegionalLeader(
            @Valid @RequestBody CreateRegionalLeaderRequest request) {
        return ApiResponse.success(regionalLeaderService.createRegionalLeader(request));
    }

    @Operation(summary = "区域负责人详情",
            description = "获取区域负责人详情。角色: property_admin, platform_admin, coordinator, sector_leader")
    @GetMapping("/{id}")
    public ApiResponse<RegionalLeaderDetailResponse> getRegionalLeader(
            @Parameter(description = "区域负责人ID") @PathVariable String id) {
        return ApiResponse.success(regionalLeaderService.getRegionalLeader(id));
    }

    @Operation(summary = "更新区域负责人",
            description = "更新区域负责人信息。角色: coordinator, property_admin, sector_leader")
    @PutMapping("/{id}")
    public ApiResponse<RegionalLeaderDetailResponse> updateRegionalLeader(
            @Parameter(description = "区域负责人ID") @PathVariable String id,
            @Valid @RequestBody UpdateRegionalLeaderRequest request) {
        return ApiResponse.success(regionalLeaderService.updateRegionalLeader(id, request));
    }

    @Operation(summary = "删除区域负责人",
            description = "删除区域负责人（软删除，置为 inactive）。角色: coordinator, property_admin, sector_leader")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRegionalLeader(
            @Parameter(description = "区域负责人ID") @PathVariable String id) {
        regionalLeaderService.deleteRegionalLeader(id);
        return ApiResponse.success(null);
    }
}
