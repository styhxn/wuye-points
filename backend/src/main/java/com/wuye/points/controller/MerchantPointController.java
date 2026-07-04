package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.merchant.point.GrantPointRequest;
import com.wuye.points.dto.merchant.point.GrantPointResponse;
import com.wuye.points.dto.merchant.point.PurchasePointRequest;
import com.wuye.points.dto.merchant.point.PurchasePointResponse;
import com.wuye.points.dto.merchant.point.PurchaseRecordItemResponse;
import com.wuye.points.service.MerchantPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家积分购买/赠送
 */
@Tag(name = "商家积分模块", description = "商家购买积分、赠送积分、购买记录")
@RestController
@RequestMapping("/merchant/points")
@RequiredArgsConstructor
public class MerchantPointController {

    private final MerchantPointService merchantPointService;

    @Operation(summary = "商家购买积分", description = "商家购买积分。Auth: Required | Roles: merchant")
    @PostMapping("/purchase")
    public ApiResponse<PurchasePointResponse> purchase(
            @Valid @RequestBody PurchasePointRequest request) {
        return ApiResponse.success(merchantPointService.purchase(request));
    }

    @Operation(summary = "购买记录列表", description = "购买记录。Auth: Required | Roles: merchant")
    @GetMapping("/purchases")
    public ApiResponse<PageResponse<PurchaseRecordItemResponse>> listPurchases(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "审核状态") @RequestParam(required = false) String auditStatus,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        return ApiResponse.success(merchantPointService.listPurchases(
                page, pageSize, auditStatus, startDate, endDate));
    }

    @Operation(summary = "商家赠送积分", description = "商家赠送积分给住户。Auth: Required | Roles: merchant")
    @PostMapping("/grant")
    public ApiResponse<GrantPointResponse> grant(
            @Valid @RequestBody GrantPointRequest request) {
        return ApiResponse.success(merchantPointService.grant(request));
    }
}
