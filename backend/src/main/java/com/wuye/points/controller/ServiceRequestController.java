package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.servicerequest.CreateServiceRequestRequest;
import com.wuye.points.dto.servicerequest.PayServiceRequestQuoteRequest;
import com.wuye.points.dto.servicerequest.ServiceRequestDetailResponse;
import com.wuye.points.dto.servicerequest.ServiceRequestItemResponse;
import com.wuye.points.service.ServiceRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "服务需求", description = "服务需求智能匹配（居民端）")
@RestController
@RequestMapping("/service-requests")
@RequiredArgsConstructor
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    @Operation(summary = "发布服务需求", description = "发布服务需求并匹配商家，需登录，角色：resident")
    @PostMapping
    public ApiResponse<ServiceRequestItemResponse> create(
            @Valid @RequestBody CreateServiceRequestRequest request) {
        return ApiResponse.success(serviceRequestService.create(request));
    }

    @Operation(summary = "我的服务需求", description = "我的服务需求列表，需登录，角色：resident")
    @GetMapping("/my")
    public ApiResponse<PageResponse<ServiceRequestItemResponse>> listMy(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(serviceRequestService.listMy(page, pageSize));
    }

    @Operation(summary = "需求详情", description = "含商家队列与费用通知，需登录，角色：resident")
    @GetMapping("/{id}")
    public ApiResponse<ServiceRequestDetailResponse> getDetail(
            @Parameter(description = "需求ID") @PathVariable String id) {
        return ApiResponse.success(serviceRequestService.getDetail(id));
    }

    @Operation(summary = "取消需求", description = "仅匹配中/协商中可取消，需登录，角色：resident")
    @PostMapping("/{id}/cancel")
    public ApiResponse<ServiceRequestItemResponse> cancel(
            @Parameter(description = "需求ID") @PathVariable String id) {
        return ApiResponse.success(serviceRequestService.cancel(id));
    }

    @Operation(summary = "确认支付报价", description = "积分/物业币/混合支付，需登录，角色：resident")
    @PostMapping("/{id}/pay-quote")
    public ApiResponse<ServiceRequestItemResponse> payQuote(
            @Parameter(description = "需求ID") @PathVariable String id,
            @Valid @RequestBody PayServiceRequestQuoteRequest request) {
        return ApiResponse.success(serviceRequestService.payQuote(id, request));
    }
}
