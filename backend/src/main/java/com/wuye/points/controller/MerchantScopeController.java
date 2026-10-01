package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.merchant.MessageServiceConfigResponse;
import com.wuye.points.dto.merchant.ServiceScopeResponse;
import com.wuye.points.dto.merchant.UpdateMessageServiceRequest;
import com.wuye.points.dto.merchant.UpdateServiceScopeRequest;
import com.wuye.points.service.MerchantScopeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家服务范围与消息服务配置
 */
@Tag(name = "商家服务范围与消息服务", description = "商家服务范围配置、消息服务开通与配置（§55 / §57.1）")
@RestController
@RequestMapping("/merchants")
@RequiredArgsConstructor
public class MerchantScopeController {

    private final MerchantScopeService merchantScopeService;

    @Operation(summary = "查询本店服务范围", description = "角色: merchant")
    @GetMapping("/my/service-scope")
    public ApiResponse<ServiceScopeResponse> getServiceScope() {
        return ApiResponse.success(merchantScopeService.getServiceScope());
    }

    @Operation(summary = "更新服务范围", description = "角色: merchant")
    @PutMapping("/my/service-scope")
    public ApiResponse<ServiceScopeResponse> updateServiceScope(
            @Valid @RequestBody UpdateServiceScopeRequest request) {
        return ApiResponse.success(merchantScopeService.updateServiceScope(request));
    }

    @Operation(summary = "查询消息服务配置", description = "角色: merchant")
    @GetMapping("/my/message-service")
    public ApiResponse<MessageServiceConfigResponse> getMessageService() {
        return ApiResponse.success(merchantScopeService.getMessageService());
    }

    @Operation(summary = "开通/配置消息服务", description = "角色: merchant。POST 与 PUT 同逻辑，兼容前端 POST 调用")
    @PutMapping("/my/message-service")
    public ApiResponse<MessageServiceConfigResponse> updateMessageService(
            @Valid @RequestBody UpdateMessageServiceRequest request) {
        return ApiResponse.success(merchantScopeService.updateMessageService(request));
    }

    @Operation(summary = "开通/配置消息服务（同 PUT）", description = "角色: merchant。兼容前端以 POST 调用")
    @PostMapping("/my/message-service")
    public ApiResponse<MessageServiceConfigResponse> updateMessageServicePost(
            @Valid @RequestBody UpdateMessageServiceRequest request) {
        return ApiResponse.success(merchantScopeService.updateMessageService(request));
    }
}
