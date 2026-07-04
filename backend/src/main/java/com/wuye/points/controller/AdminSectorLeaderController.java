package com.wuye.points.controller;

import com.wuye.points.dto.admin.sectorleader.AppointSectorLeaderRequest;
import com.wuye.points.dto.admin.sectorleader.SectorLeaderDetailResponse;
import com.wuye.points.dto.admin.sectorleader.SectorLeaderListItemResponse;
import com.wuye.points.dto.admin.sectorleader.UpdateSectorLeaderRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminSectorLeaderService;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

/**
 * 板块负责人管理模块（管理端）
 */
@Tag(name = "管理端 — 板块负责人管理", description = "板块负责人列表、指派、详情、更新、撤销")
@RestController
@RequestMapping("/admin/sector-leaders")
@RequiredArgsConstructor
public class AdminSectorLeaderController {

    private final AdminSectorLeaderService adminSectorLeaderService;

    @Operation(summary = "板块负责人列表", description = "获取板块负责人列表。角色: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<PageResponse<SectorLeaderListItemResponse>> listSectorLeaders(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "板块名称") @RequestParam(required = false) String sector,
            @Parameter(description = "统筹负责人ID") @RequestParam(required = false) String coordinatorId,
            @Parameter(description = "板块负责人状态") @RequestParam(required = false) String status,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminSectorLeaderService.listSectorLeaders(
                page, pageSize, sector, coordinatorId, status, propertyCompanyId));
    }

    @Operation(summary = "指派板块负责人", description = "指派板块负责人。角色: property_admin, platform_admin")
    @PostMapping
    public ApiResponse<SectorLeaderDetailResponse> appointSectorLeader(
            @Valid @RequestBody AppointSectorLeaderRequest request) {
        return ApiResponse.success(adminSectorLeaderService.appointSectorLeader(request));
    }

    @Operation(summary = "板块负责人详情", description = "获取板块负责人详情。角色: property_admin, platform_admin")
    @GetMapping("/{id}")
    public ApiResponse<SectorLeaderDetailResponse> getSectorLeader(
            @Parameter(description = "资源ID") @PathVariable String id) {
        return ApiResponse.success(adminSectorLeaderService.getSectorLeader(id));
    }

    @Operation(summary = "更新板块负责人", description = "更新板块负责人信息。角色: property_admin, platform_admin")
    @PutMapping("/{id}")
    public ApiResponse<SectorLeaderDetailResponse> updateSectorLeader(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Valid @RequestBody UpdateSectorLeaderRequest request) {
        return ApiResponse.success(adminSectorLeaderService.updateSectorLeader(id, request));
    }

    @Operation(summary = "撤销板块负责人", description = "撤销板块负责人。角色: property_admin, platform_admin")
    @DeleteMapping("/{id}")
    public ApiResponse<SectorLeaderDetailResponse> revokeSectorLeader(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Parameter(description = "撤销原因") @RequestParam String reason) {
        return ApiResponse.success(adminSectorLeaderService.revokeSectorLeader(id, reason));
    }
}
