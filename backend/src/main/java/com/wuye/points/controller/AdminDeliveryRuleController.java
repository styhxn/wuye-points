package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.delivery.CreateDeliveryRuleRequest;
import com.wuye.points.dto.delivery.DeliveryRuleResponse;
import com.wuye.points.dto.delivery.UpdateDeliveryRuleRequest;
import com.wuye.points.service.DeliveryRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端 — 配送规则", description = "配送规则列表、详情、创建、更新")
@RestController
@RequestMapping("/admin/delivery-rules")
@RequiredArgsConstructor
public class AdminDeliveryRuleController {

    private final DeliveryRuleService deliveryRuleService;

    @Operation(summary = "配送规则列表")
    @GetMapping
    public ApiResponse<PageResponse<DeliveryRuleResponse>> listRules(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(deliveryRuleService.listRules(page, pageSize, propertyCompanyId));
    }

    @Operation(summary = "配送规则详情")
    @GetMapping("/{id}")
    public ApiResponse<DeliveryRuleResponse> getRule(
            @Parameter(description = "配送规则ID") @PathVariable String id) {
        return ApiResponse.success(deliveryRuleService.getRule(id));
    }

    @Operation(summary = "创建配送规则")
    @PostMapping
    public ApiResponse<DeliveryRuleResponse> createRule(
            @Valid @RequestBody CreateDeliveryRuleRequest request) {
        return ApiResponse.success(deliveryRuleService.createRule(request));
    }

    @Operation(summary = "更新配送规则")
    @PutMapping("/{id}")
    public ApiResponse<DeliveryRuleResponse> updateRule(
            @Parameter(description = "配送规则ID") @PathVariable String id,
            @Valid @RequestBody UpdateDeliveryRuleRequest request) {
        return ApiResponse.success(deliveryRuleService.updateRule(id, request));
    }
}
