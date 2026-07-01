package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.delivery.CreateDeliveryRuleRequest;
import com.wuye.points.dto.delivery.DeliveryRuleResponse;
import com.wuye.points.dto.delivery.UpdateDeliveryRuleRequest;
import com.wuye.points.service.DeliveryRuleService;
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

/**
 * 配送规则管理
 */
@RestController
@RequestMapping("/delivery-rules")
@RequiredArgsConstructor
public class DeliveryRuleController {

    private final DeliveryRuleService deliveryRuleService;

    @GetMapping
    public ApiResponse<PageResponse<DeliveryRuleResponse>> listRules(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(deliveryRuleService.listRules(page, pageSize, propertyCompanyId));
    }

    @GetMapping("/{id}")
    public ApiResponse<DeliveryRuleResponse> getRule(@PathVariable String id) {
        return ApiResponse.success(deliveryRuleService.getRule(id));
    }

    @PostMapping
    public ApiResponse<DeliveryRuleResponse> createRule(
            @Valid @RequestBody CreateDeliveryRuleRequest request) {
        return ApiResponse.success(deliveryRuleService.createRule(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DeliveryRuleResponse> updateRule(
            @PathVariable String id,
            @Valid @RequestBody UpdateDeliveryRuleRequest request) {
        return ApiResponse.success(deliveryRuleService.updateRule(id, request));
    }
}
