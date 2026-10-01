package com.wuye.points.controller;

import com.wuye.points.dto.admin.deliverypricerange.CreateDeliveryPriceRangeRequest;
import com.wuye.points.dto.admin.deliverypricerange.DeliveryPriceRangeResponse;
import com.wuye.points.dto.admin.deliverypricerange.UpdateDeliveryPriceRangeRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.service.AdminDeliveryPriceRangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;

/**
 * 管理端 — 物业公司配送价格区间管理
 */
@Tag(name = "管理端 — 配送价格区间", description = "物业公司配送价格区间列表、创建、更新、删除")
@RestController
@RequestMapping("/admin/delivery-price-ranges")
@RequiredArgsConstructor
public class AdminDeliveryPriceRangeController {

    private final AdminDeliveryPriceRangeService adminDeliveryPriceRangeService;

    @Operation(summary = "配送价格区间列表", description = "查询物业公司价格区间。角色: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<List<DeliveryPriceRangeResponse>> listRanges(
            @Parameter(description = "物业公司ID（platform_admin 可指定）") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "距离类型（1km/3km/5km/citywide/any）")
            @RequestParam(required = false) String distanceType,
            @Parameter(description = "配送范围（in_community/out_community/both）")
            @RequestParam(required = false) String deliveryScope) {
        return ApiResponse.success(adminDeliveryPriceRangeService
                .listRanges(propertyCompanyId, status, distanceType, deliveryScope));
    }

    @Operation(summary = "创建配送价格区间", description = "创建价格区间，会校验与现有区间不重叠。角色: property_admin, platform_admin")
    @PostMapping
    public ApiResponse<DeliveryPriceRangeResponse> createRange(
            @Valid @RequestBody CreateDeliveryPriceRangeRequest request) {
        return ApiResponse.success(adminDeliveryPriceRangeService.createRange(request));
    }

    @Operation(summary = "更新配送价格区间", description = "更新价格区间。角色: property_admin, platform_admin")
    @PutMapping("/{id}")
    public ApiResponse<DeliveryPriceRangeResponse> updateRange(
            @Parameter(description = "区间ID") @PathVariable String id,
            @Valid @RequestBody UpdateDeliveryPriceRangeRequest request) {
        return ApiResponse.success(adminDeliveryPriceRangeService.updateRange(id, request));
    }

    @Operation(summary = "删除配送价格区间", description = "逻辑删除（设为inactive）。角色: property_admin, platform_admin")
    @DeleteMapping("/{id}")
    public ApiResponse<DeliveryPriceRangeResponse> deleteRange(
            @Parameter(description = "区间ID") @PathVariable String id) {
        return ApiResponse.success(adminDeliveryPriceRangeService.deleteRange(id));
    }
}
