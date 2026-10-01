package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.entity.RegionQuota;
import com.wuye.points.service.AdminRegionQuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "区域配额模块", description = "区域/板块额度配额维护")
@RestController
@RequestMapping("/admin/region-quotas")
@RequiredArgsConstructor
public class AdminRegionQuotaController {

    private final AdminRegionQuotaService adminRegionQuotaService;

    @Operation(summary = "配额列表", description = "可按板块过滤。Roles: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<List<RegionQuota>> list(
            @Parameter(description = "板块/区域标识") @RequestParam(required = false) String sector) {
        return ApiResponse.success(adminRegionQuotaService.list(sector));
    }

    @Operation(summary = "新增配额", description = "Roles: property_admin, platform_admin")
    @PostMapping
    public ApiResponse<RegionQuota> create(@RequestBody RegionQuota request) {
        return ApiResponse.success(adminRegionQuotaService.create(request));
    }

    @Operation(summary = "修改配额", description = "仅更新非 null 字段，配额总量不可低于已使用量。Roles: property_admin, platform_admin")
    @PutMapping("/{id}")
    public ApiResponse<RegionQuota> update(
            @Parameter(description = "配额ID") @PathVariable String id,
            @RequestBody RegionQuota request) {
        return ApiResponse.success(adminRegionQuotaService.update(id, request));
    }

    @Operation(summary = "删除配额", description = "Roles: property_admin, platform_admin")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@Parameter(description = "配额ID") @PathVariable String id) {
        adminRegionQuotaService.delete(id);
        return ApiResponse.success(null);
    }
}
