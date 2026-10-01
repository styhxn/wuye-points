package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.communityentity.BindPropertyCompanyRequest;
import com.wuye.points.dto.communityentity.CommunityEntityDetailResponse;
import com.wuye.points.dto.communityentity.CommunityEntityListItemResponse;
import com.wuye.points.dto.communityentity.CommunityPropertyBindingItemResponse;
import com.wuye.points.dto.communityentity.CreateCommunityEntityRequest;
import com.wuye.points.dto.communityentity.UpdateCommunityPropertyPermissionRequest;
import com.wuye.points.service.AdminCommunityEntityService;
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

@Tag(name = "管理端 — 社区多物业绑定", description = "社区实体与物业公司绑定管理")
@RestController
@RequestMapping("/admin/community-entities")
@RequiredArgsConstructor
public class AdminCommunityEntityController {

    private final AdminCommunityEntityService adminCommunityEntityService;

    @Operation(summary = "社区列表", description = "角色: platform_admin, property_admin（仅可见已绑定）")
    @GetMapping
    public ApiResponse<PageResponse<CommunityEntityListItemResponse>> listCommunityEntities(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminCommunityEntityService.listCommunityEntities(page, pageSize, keyword));
    }

    @Operation(summary = "创建社区", description = "角色: platform_admin")
    @PostMapping
    public ApiResponse<CommunityEntityDetailResponse> createCommunityEntity(
            @Valid @RequestBody CreateCommunityEntityRequest request) {
        return ApiResponse.success(adminCommunityEntityService.createCommunityEntity(request));
    }

    @Operation(summary = "已绑定物业列表", description = "角色: platform_admin, property_admin")
    @GetMapping("/{id}/property-companies")
    public ApiResponse<List<CommunityPropertyBindingItemResponse>> listPropertyBindings(
            @Parameter(description = "社区ID") @PathVariable String id) {
        return ApiResponse.success(adminCommunityEntityService.listPropertyBindings(id));
    }

    @Operation(summary = "绑定物业公司", description = "角色: platform_admin")
    @PostMapping("/{id}/property-companies")
    public ApiResponse<CommunityPropertyBindingItemResponse> bindPropertyCompany(
            @Parameter(description = "社区ID") @PathVariable String id,
            @Valid @RequestBody BindPropertyCompanyRequest request) {
        return ApiResponse.success(adminCommunityEntityService.bindPropertyCompany(id, request));
    }

    @Operation(summary = "解绑物业公司", description = "角色: platform_admin")
    @DeleteMapping("/{id}/property-companies/{propertyCompanyId}")
    public ApiResponse<Void> unbindPropertyCompany(
            @Parameter(description = "社区ID") @PathVariable String id,
            @Parameter(description = "物业公司ID") @PathVariable String propertyCompanyId) {
        adminCommunityEntityService.unbindPropertyCompany(id, propertyCompanyId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "调整绑定权限", description = "角色: platform_admin")
    @PutMapping("/{id}/property-companies/{propertyCompanyId}/permissions")
    public ApiResponse<CommunityPropertyBindingItemResponse> updatePermissions(
            @Parameter(description = "社区ID") @PathVariable String id,
            @Parameter(description = "物业公司ID") @PathVariable String propertyCompanyId,
            @Valid @RequestBody UpdateCommunityPropertyPermissionRequest request) {
        return ApiResponse.success(adminCommunityEntityService.updatePermissions(id, propertyCompanyId, request));
    }
}
