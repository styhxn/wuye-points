package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.merchant.point.GrantPointRequest;
import com.wuye.points.dto.merchant.point.GrantPointResponse;
import com.wuye.points.dto.merchant.point.PurchasePointRequest;
import com.wuye.points.dto.merchant.point.PurchasePointResponse;
import com.wuye.points.dto.merchant.point.PurchaseRecordItemResponse;
import com.wuye.points.service.MerchantPointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家积分购买/赠送
 */
@RestController
@RequestMapping("/merchant/points")
@RequiredArgsConstructor
public class MerchantPointController {

    private final MerchantPointService merchantPointService;

    /**
     * 商家购买积分
     * Auth: Required | Roles: merchant
     */
    @PostMapping("/purchase")
    public ApiResponse<PurchasePointResponse> purchase(
            @Valid @RequestBody PurchasePointRequest request) {
        return ApiResponse.success(merchantPointService.purchase(request));
    }

    /**
     * 购买记录
     * Auth: Required | Roles: merchant
     */
    @GetMapping("/purchases")
    public ApiResponse<PageResponse<PurchaseRecordItemResponse>> listPurchases(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String auditStatus,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ApiResponse.success(merchantPointService.listPurchases(
                page, pageSize, auditStatus, startDate, endDate));
    }

    /**
     * 商家赠送积分给住户
     * Auth: Required | Roles: merchant
     */
    @PostMapping("/grant")
    public ApiResponse<GrantPointResponse> grant(
            @Valid @RequestBody GrantPointRequest request) {
        return ApiResponse.success(merchantPointService.grant(request));
    }
}
