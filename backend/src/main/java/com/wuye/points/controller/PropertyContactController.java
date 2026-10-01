package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.propertycontact.PropertyContactPublicResponse;
import com.wuye.points.service.PropertyContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "物业联系方式（前端）", description = "用户未绑定楼栋时展示的物业联系方式")
@RestController
@RequestMapping("/property-contact")
@RequiredArgsConstructor
public class PropertyContactController {

    private final PropertyContactService propertyContactService;

    @Operation(summary = "获取物业联系方式（前端展示）",
            description = "匿名可访问；仅当管理端已配置且启用时返回数据，否则返回 null。前端在用户未绑定楼栋时调用并展示。")
    @GetMapping
    public ApiResponse<PropertyContactPublicResponse> getPublicConfig() {
        return ApiResponse.success(propertyContactService.getPublicConfig());
    }
}
