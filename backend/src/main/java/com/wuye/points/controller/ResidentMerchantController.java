package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.resident.ConsumedMerchantListItemResponse;
import com.wuye.points.service.ResidentMerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

/**
 * 居民消费商家模块
 */
@Tag(name = "消费过的商家模块", description = "业主消费过的商家列表")
@RestController
@RequestMapping("/residents")
@RequiredArgsConstructor
public class ResidentMerchantController {

    private final ResidentMerchantService residentMerchantService;

    /**
     * 我消费过的商家列表
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "消费过的商家列表", description = "我消费过的商家列表。角色: resident")
    @GetMapping("/me/consumed-merchants")
    public ApiResponse<PageResponse<ConsumedMerchantListItemResponse>> listConsumedMerchants(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(residentMerchantService.listConsumedMerchants(page, pageSize));
    }
}
