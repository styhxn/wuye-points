package com.wuye.points.controller;

import com.wuye.points.dto.admin.individualleader.AppointIndividualLeaderRequest;
import com.wuye.points.dto.admin.individualleader.IndividualLeaderDetailResponse;
import com.wuye.points.dto.admin.individualleader.IndividualLeaderListItemResponse;
import com.wuye.points.dto.admin.individualleader.UpdateIndividualLeaderRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminIndividualLeaderService;
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
 * 个体负责人管理模块（管理端）
 */
@Tag(name = "管理端 — 个体负责人管理", description = "个体负责人列表、指派、详情、更新、撤销")
@RestController
@RequestMapping("/admin/individual-leaders")
@RequiredArgsConstructor
public class AdminIndividualLeaderController {

    private final AdminIndividualLeaderService adminIndividualLeaderService;

    @Operation(summary = "个体负责人列表", description = "获取个体负责人列表。角色: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<PageResponse<IndividualLeaderListItemResponse>> listIndividualLeaders(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "板块负责人ID") @RequestParam(required = false) String sectorLeaderId,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "个体负责人状态") @RequestParam(required = false) String status,
            @Parameter(description = "板块名称") @RequestParam(required = false) String sector,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminIndividualLeaderService.listIndividualLeaders(
                page, pageSize, sectorLeaderId, keyword, status, sector, propertyCompanyId));
    }

    @Operation(summary = "指派个体负责人", description = "指派个体负责人。角色: property_admin, platform_admin")
    @PostMapping
    public ApiResponse<IndividualLeaderDetailResponse> appointIndividualLeader(
            @Valid @RequestBody AppointIndividualLeaderRequest request) {
        return ApiResponse.success(adminIndividualLeaderService.appointIndividualLeader(request));
    }

    @Operation(summary = "个体负责人详情", description = "获取个体负责人详情。角色: property_admin, platform_admin")
    @GetMapping("/{id}")
    public ApiResponse<IndividualLeaderDetailResponse> getIndividualLeader(
            @Parameter(description = "资源ID") @PathVariable String id) {
        return ApiResponse.success(adminIndividualLeaderService.getIndividualLeader(id));
    }

    @Operation(summary = "更新个体负责人", description = "更新个体负责人信息。角色: property_admin, platform_admin")
    @PutMapping("/{id}")
    public ApiResponse<IndividualLeaderDetailResponse> updateIndividualLeader(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Valid @RequestBody UpdateIndividualLeaderRequest request) {
        return ApiResponse.success(adminIndividualLeaderService.updateIndividualLeader(id, request));
    }

    @Operation(summary = "撤销个体负责人", description = "撤销个体负责人。角色: property_admin, platform_admin")
    @DeleteMapping("/{id}")
    public ApiResponse<IndividualLeaderDetailResponse> revokeIndividualLeader(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Parameter(description = "撤销原因") @RequestParam String reason) {
        return ApiResponse.success(adminIndividualLeaderService.revokeIndividualLeader(id, reason));
    }
}
