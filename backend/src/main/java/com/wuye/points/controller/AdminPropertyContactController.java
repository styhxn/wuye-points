package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.propertycontact.PropertyContactDetailResponse;
import com.wuye.points.dto.propertycontact.UpsertPropertyContactRequest;
import com.wuye.points.service.PropertyContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "物业联系方式（管理端）", description = "配置与查询物业联系方式，前端在用户未绑定楼栋时展示")
@RestController
@RequestMapping("/admin/property-contact")
@RequiredArgsConstructor
public class AdminPropertyContactController {

    private final PropertyContactService propertyContactService;

    @Operation(summary = "获取物业联系方式配置", description = "角色：property_admin / platform_admin")
    @GetMapping
    public ApiResponse<PropertyContactDetailResponse> getConfig() {
        return ApiResponse.success(propertyContactService.getConfig());
    }

    @Operation(summary = "配置/更新物业联系方式",
            description = "全局单条配置；若已存在则覆盖更新。角色：property_admin / platform_admin")
    @PostMapping
    public ApiResponse<PropertyContactDetailResponse> upsertConfig(
            @Valid @RequestBody UpsertPropertyContactRequest request) {
        return ApiResponse.success(propertyContactService.upsertConfig(request));
    }
}
