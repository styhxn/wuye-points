package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.servicerequest.ServiceCategoryItemResponse;
import com.wuye.points.service.ServiceRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "服务分类", description = "服务需求分类字典")
@RestController
@RequestMapping("/service-categories")
@RequiredArgsConstructor
public class ServiceCategoryController {

    private final ServiceRequestService serviceRequestService;

    @Operation(summary = "服务分类列表", description = "获取服务分类字典，无需鉴权")
    @GetMapping
    public ApiResponse<Map<String, List<ServiceCategoryItemResponse>>> listCategories() {
        return ApiResponse.success(Map.of("list", serviceRequestService.listCategories()));
    }
}
