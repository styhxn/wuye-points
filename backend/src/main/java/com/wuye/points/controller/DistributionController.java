package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.distribution.DistributionCalculateResponse;
import com.wuye.points.dto.distribution.DistributionRecordItemResponse;
import com.wuye.points.dto.distribution.DistributionStatsResponse;
import com.wuye.points.service.DistributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 分成模块（小程序端）
 */
@RestController
@RequiredArgsConstructor
public class DistributionController {

    private final DistributionService distributionService;

    /**
     * 分成记录列表
     * Auth: Required | Roles: coordinator, property_admin, platform_admin
     */
    @GetMapping("/distribution/records")
    public ApiResponse<PageResponse<DistributionRecordItemResponse>> listRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) String coordinatorId,
            @RequestParam(required = false) String sectorLeaderId,
            @RequestParam(required = false) String individualLeaderId,
            @RequestParam(required = false) String propertyCompanyId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ApiResponse.success(distributionService.listRecords(
                page, pageSize, orderId, merchantId, coordinatorId, sectorLeaderId,
                individualLeaderId, propertyCompanyId, startDate, endDate));
    }

    /**
     * 分成统计
     * Auth: Required | Roles: coordinator, property_admin, platform_admin
     */
    @GetMapping("/distribution/stats")
    public ApiResponse<DistributionStatsResponse> getStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "summary") String dimension,
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(distributionService.getStats(startDate, endDate, dimension, propertyCompanyId));
    }

    /**
     * 试算分成（预览，不落库）
     */
    @GetMapping("/distribution/calculate")
    public ApiResponse<DistributionCalculateResponse> calculatePreview(
            @RequestParam String merchantId,
            @RequestParam BigDecimal totalAmount,
            @RequestParam(required = false) Integer pointUsed,
            @RequestParam(required = false) BigDecimal coinUsed,
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(distributionService.calculatePreview(
                merchantId, totalAmount, pointUsed, coinUsed, propertyCompanyId));
    }

    /**
     * 分成记录查询（管理端路径，对齐 docs/03 16.11）
     */
    @GetMapping("/admin/distribution-records")
    public ApiResponse<PageResponse<DistributionRecordItemResponse>> adminListRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) String coordinatorId,
            @RequestParam(required = false) String sectorLeaderId,
            @RequestParam(required = false) String individualLeaderId,
            @RequestParam(required = false) String propertyCompanyId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ApiResponse.success(distributionService.listRecords(
                page, pageSize, orderId, merchantId, coordinatorId, sectorLeaderId,
                individualLeaderId, propertyCompanyId, startDate, endDate));
    }

    /**
     * 分成统计（管理端路径，对齐 docs/03 16.12）
     */
    @GetMapping("/admin/distribution/statistics")
    public ApiResponse<DistributionStatsResponse> adminGetStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "summary") String dimension,
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(distributionService.getStats(startDate, endDate, dimension, propertyCompanyId));
    }
}
