package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.resident.BuildingChangeListItemResponse;
import com.wuye.points.dto.resident.BuildingChangeResponse;
import com.wuye.points.service.ResidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 楼栋变更审批（管理端）
 */
@Tag(name = "管理端-楼栋变更审批", description = "楼栋变更申请列表与审批")
@RestController
@RequestMapping("/admin/building-changes")
@RequiredArgsConstructor
public class AdminBuildingChangeController {

    private final ResidentService residentService;

    @Operation(summary = "楼栋变更申请列表", description = "楼栋变更申请列表。角色: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<PageResponse<BuildingChangeListItemResponse>> listBuildingChanges(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "状态筛选 pending/approved/rejected") @RequestParam(required = false) String status,
            @Parameter(description = "物业公司ID（platform_admin可指定）") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(residentService.listBuildingChanges(page, pageSize, status, propertyCompanyId));
    }

    @Operation(summary = "通过楼栋变更申请", description = "审批通过楼栋变更申请，通过后写入住户楼栋地址。角色: property_admin, platform_admin")
    @PostMapping("/{id}/approve")
    public ApiResponse<BuildingChangeResponse> approve(
            @Parameter(description = "申请ID", example = "bcr_xxx") @PathVariable String id) {
        return ApiResponse.success(residentService.auditBuildingChange(id, true, null));
    }

    @Operation(summary = "驳回楼栋变更申请", description = "驳回楼栋变更申请，必须填写驳回原因。角色: property_admin, platform_admin")
    @PostMapping("/{id}/reject")
    public ApiResponse<BuildingChangeResponse> reject(
            @Parameter(description = "申请ID", example = "bcr_xxx") @PathVariable String id,
            @Parameter(description = "驳回原因") @RequestParam String reason) {
        return ApiResponse.success(residentService.auditBuildingChange(id, false, reason));
    }
}
