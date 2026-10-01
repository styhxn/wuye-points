package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.servicerequest.MerchantPendingServiceRequestResponse;
import com.wuye.points.dto.servicerequest.ServiceRequestItemResponse;
import com.wuye.points.dto.servicerequest.ServiceRequestQuoteRequest;
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

@Tag(name = "商家服务需求", description = "商家待响应服务需求")
@RestController
@RequestMapping("/merchant/service-requests")
@RequiredArgsConstructor
public class MerchantServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    @Operation(summary = "待响应列表", description = "商家待响应服务需求列表，需登录，角色：merchant")
    @GetMapping("/pending")
    public ApiResponse<PageResponse<MerchantPendingServiceRequestResponse>> listPending(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(serviceRequestService.listMerchantPending(page, pageSize));
    }

    @Operation(summary = "接单", description = "5分钟内响应，需登录，角色：merchant")
    @PostMapping("/{id}/accept")
    public ApiResponse<ServiceRequestItemResponse> accept(
            @Parameter(description = "需求ID") @PathVariable String id) {
        return ApiResponse.success(serviceRequestService.merchantAccept(id));
    }

    @Operation(summary = "跳过", description = "跳过当前服务需求，需登录，角色：merchant")
    @PostMapping("/{id}/skip")
    public ApiResponse<ServiceRequestItemResponse> skip(
            @Parameter(description = "需求ID") @PathVariable String id) {
        return ApiResponse.success(serviceRequestService.merchantSkip(id));
    }

    @Operation(summary = "发送费用通知", description = "协商后发送报价，需登录，角色：merchant")
    @PostMapping("/{id}/quote")
    public ApiResponse<ServiceRequestItemResponse> quote(
            @Parameter(description = "需求ID") @PathVariable String id,
            @Valid @RequestBody ServiceRequestQuoteRequest request) {
        return ApiResponse.success(serviceRequestService.merchantQuote(id, request));
    }
}
