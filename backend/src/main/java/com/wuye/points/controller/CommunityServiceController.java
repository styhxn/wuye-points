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

/**
 * 社区服务模块
 */
@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class CommunityServiceController {

    private final CommunityServiceService communityServiceService;

    @GetMapping("/my-subscriptions")
    public ApiResponse<PageResponse<ServiceSubscriptionItemResponse>> listMySubscriptions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(communityServiceService.listMySubscriptions(page, pageSize, status));
    }

    @GetMapping
    public ApiResponse<PageResponse<CommunityServiceListItemResponse>> listServices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean mine,
            @RequestParam(defaultValue = "+rankOrder") String sort) {
        return ApiResponse.success(communityServiceService.listServices(page, pageSize, category, mine, sort));
    }

    @PostMapping
    public ApiResponse<CommunityServiceDetailResponse> createService(
            @Valid @RequestBody CreateCommunityServiceRequest request) {
        return ApiResponse.success(communityServiceService.createService(request));
    }

    @PostMapping("/{id}/subscribe")
    public ApiResponse<SubscribeServiceResponse> subscribe(
            @PathVariable String id,
            @Valid @RequestBody SubscribeServiceRequest request) {
        return ApiResponse.success(communityServiceService.subscribe(id, request));
    }

    @PostMapping("/{id}/book")
    public ApiResponse<ServiceOrderDetailResponse> bookService(
            @PathVariable String id,
            @Valid @RequestBody BookServiceRequest request) {
        return ApiResponse.success(communityServiceService.bookService(id, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CommunityServiceDetailResponse> updateService(
            @PathVariable String id,
            @Valid @RequestBody UpdateCommunityServiceRequest request) {
        return ApiResponse.success(communityServiceService.updateService(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<CommunityServiceDetailResponse> getService(@PathVariable String id) {
        return ApiResponse.success(communityServiceService.getService(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<DeleteCommunityServiceResponse> deleteService(@PathVariable String id) {
        return ApiResponse.success(communityServiceService.deleteService(id));
    }
}
