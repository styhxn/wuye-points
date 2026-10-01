package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.entity.NavigationItem;
import com.wuye.points.service.AdminNavigationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "导航栏配置模块", description = "手机端导航栏拉取")
@RestController
@RequestMapping("/navigation-items")
@RequiredArgsConstructor
public class NavigationController {

    private final AdminNavigationService adminNavigationService;

    @Operation(summary = "获取手机端导航", description = "返回启用中的导航项，可按模块过滤(home/service/activity/resident_merchant)，优先物业公司配置，未配置回落平台默认")
    @GetMapping
    public ApiResponse<List<NavigationItem>> listForApp(
            @io.swagger.v3.oas.annotations.Parameter(description = "模块过滤") @org.springframework.web.bind.annotation.RequestParam(required = false) String module) {
        return ApiResponse.success(adminNavigationService.listForApp(module));
    }
}
