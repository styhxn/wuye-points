package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.entity.NavigationItem;
import com.wuye.points.service.AdminNavigationService;
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

@Tag(name = "导航栏配置模块", description = "手机端导航栏配置维护（后台）")
@RestController
@RequestMapping("/admin/navigation-items")
@RequiredArgsConstructor
public class AdminNavigationController {

    private final AdminNavigationService adminNavigationService;

    @Operation(summary = "导航项列表", description = "Roles: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<List<NavigationItem>> list(
            @Parameter(description = "物业公司ID（platform_admin 可传）") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminNavigationService.list(propertyCompanyId));
    }

    @Operation(summary = "新增导航项", description = "Roles: property_admin, platform_admin")
    @PostMapping
    public ApiResponse<NavigationItem> create(@RequestBody NavigationItem request) {
        return ApiResponse.success(adminNavigationService.create(request));
    }

    @Operation(summary = "修改导航项", description = "Roles: property_admin, platform_admin")
    @PutMapping("/{id}")
    public ApiResponse<NavigationItem> update(
            @Parameter(description = "导航项ID") @PathVariable String id,
            @RequestBody NavigationItem request) {
        return ApiResponse.success(adminNavigationService.update(id, request));
    }

    @Operation(summary = "删除导航项", description = "Roles: property_admin, platform_admin")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@Parameter(description = "导航项ID") @PathVariable String id) {
        adminNavigationService.delete(id);
        return ApiResponse.success(null);
    }
}
