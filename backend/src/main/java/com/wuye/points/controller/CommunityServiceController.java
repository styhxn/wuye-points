package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.service.BookServiceRequest;
import com.wuye.points.dto.service.CommunityServiceDetailResponse;
import com.wuye.points.dto.service.CommunityServiceListItemResponse;
import com.wuye.points.dto.service.CreateCommunityServiceRequest;
import com.wuye.points.dto.service.DeleteCommunityServiceResponse;
import com.wuye.points.dto.service.ServiceOrderDetailResponse;
import com.wuye.points.dto.service.ServiceSubscriptionItemResponse;
import com.wuye.points.dto.service.SubscribeServiceRequest;
import com.wuye.points.dto.service.SubscribeServiceResponse;
import com.wuye.points.dto.service.UpdateCommunityServiceRequest;
import com.wuye.points.service.CommunityServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

/**
 * 社区服务模块
 */
@Tag(name = "社区服务模块", description = "服务列表、订阅、预约、创建、更新、删除")
@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class CommunityServiceController {

    private final CommunityServiceService communityServiceService;

    @Operation(summary = "我订阅的服务", description = "获取当前用户订阅的社区服务列表")
    @GetMapping("/my-subscriptions")
    public ApiResponse<PageResponse<ServiceSubscriptionItemResponse>> listMySubscriptions(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "订阅状态") @RequestParam(required = false) String status) {
        return ApiResponse.success(communityServiceService.listMySubscriptions(page, pageSize, status));
    }

    @Operation(summary = "服务列表", description = "获取社区服务列表（分页/筛选）")
    @GetMapping
    public ApiResponse<PageResponse<CommunityServiceListItemResponse>> listServices(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "服务分类") @RequestParam(required = false) String category,
            @Parameter(description = "是否只看自己发布的") @RequestParam(required = false) Boolean mine,
            @Parameter(description = "排序字段（-降序，+升序）", example = "+rankOrder") @RequestParam(defaultValue = "+rankOrder") String sort) {
        return ApiResponse.success(communityServiceService.listServices(page, pageSize, category, mine, sort));
    }

    @Operation(summary = "创建服务", description = "创建社区服务")
    @PostMapping
    public ApiResponse<CommunityServiceDetailResponse> createService(
            @Valid @RequestBody CreateCommunityServiceRequest request) {
        return ApiResponse.success(communityServiceService.createService(request));
    }

    @Operation(summary = "订阅服务", description = "订阅指定社区服务，接收服务更新通知")
    @PostMapping("/{id}/subscribe")
    public ApiResponse<SubscribeServiceResponse> subscribe(
            @Parameter(description = "服务ID") @PathVariable String id,
            @Valid @RequestBody SubscribeServiceRequest request) {
        return ApiResponse.success(communityServiceService.subscribe(id, request));
    }

    @Operation(summary = "预约服务", description = "预约指定社区服务，生成服务订单")
    @PostMapping("/{id}/book")
    public ApiResponse<ServiceOrderDetailResponse> bookService(
            @Parameter(description = "服务ID") @PathVariable String id,
            @Valid @RequestBody BookServiceRequest request) {
        return ApiResponse.success(communityServiceService.bookService(id, request));
    }

    @Operation(summary = "更新服务", description = "更新社区服务信息")
    @PutMapping("/{id}")
    public ApiResponse<CommunityServiceDetailResponse> updateService(
            @Parameter(description = "服务ID") @PathVariable String id,
            @Valid @RequestBody UpdateCommunityServiceRequest request) {
        return ApiResponse.success(communityServiceService.updateService(id, request));
    }

    @Operation(summary = "服务详情", description = "获取社区服务详细信息")
    @GetMapping("/{id}")
    public ApiResponse<CommunityServiceDetailResponse> getService(
            @Parameter(description = "服务ID") @PathVariable String id) {
        return ApiResponse.success(communityServiceService.getService(id));
    }

    @Operation(summary = "删除服务", description = "删除指定社区服务")
    @DeleteMapping("/{id}")
    public ApiResponse<DeleteCommunityServiceResponse> deleteService(
            @Parameter(description = "服务ID") @PathVariable String id) {
        return ApiResponse.success(communityServiceService.deleteService(id));
    }
}
