package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.distribution.DistributionCalculateResponse;
import com.wuye.points.dto.distribution.DistributionRecordItemResponse;
import com.wuye.points.dto.distribution.DistributionStatsResponse;
import com.wuye.points.service.DistributionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 分成模块（小程序端）
 */
@Tag(name = "分成模块", description = "分成记录、统计、试算")
@RestController
@RequiredArgsConstructor
public class DistributionController {

    private final DistributionService distributionService;

    @Operation(summary = "分成记录列表", description = "分成记录列表。Auth: Required | Roles: coordinator, property_admin, platform_admin")
    @GetMapping("/distribution/records")
    public ApiResponse<PageResponse<DistributionRecordItemResponse>> listRecords(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "订单ID") @RequestParam(required = false) String orderId,
            @Parameter(description = "商家ID") @RequestParam(required = false) String merchantId,
            @Parameter(description = "协调员ID") @RequestParam(required = false) String coordinatorId,
            @Parameter(description = "片区长ID") @RequestParam(required = false) String sectorLeaderId,
            @Parameter(description = "楼栋长ID") @RequestParam(required = false) String individualLeaderId,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        return ApiResponse.success(distributionService.listRecords(
                page, pageSize, orderId, merchantId, coordinatorId, sectorLeaderId,
                individualLeaderId, propertyCompanyId, startDate, endDate));
    }

    @Operation(summary = "分成统计", description = "分成统计。Auth: Required | Roles: coordinator, property_admin, platform_admin")
    @GetMapping("/distribution/stats")
    public ApiResponse<DistributionStatsResponse> getStats(
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "统计维度", example = "summary") @RequestParam(defaultValue = "summary") String dimension,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(distributionService.getStats(startDate, endDate, dimension, propertyCompanyId));
    }

    @Operation(summary = "分成试算预览", description = "试算分成（预览，不落库）")
    @GetMapping("/distribution/calculate")
    public ApiResponse<DistributionCalculateResponse> calculatePreview(
            @Parameter(description = "商家ID") @RequestParam String merchantId,
            @Parameter(description = "订单总金额") @RequestParam BigDecimal totalAmount,
            @Parameter(description = "使用积分数") @RequestParam(required = false) Integer pointUsed,
            @Parameter(description = "使用物业币数") @RequestParam(required = false) BigDecimal coinUsed,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(distributionService.calculatePreview(
                merchantId, totalAmount, pointUsed, coinUsed, propertyCompanyId));
    }

    @Operation(summary = "管理端分成记录", description = "分成记录查询（管理端路径，对齐 docs/03 16.11）")
    @GetMapping("/admin/distribution-records")
    public ApiResponse<PageResponse<DistributionRecordItemResponse>> adminListRecords(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "订单ID") @RequestParam(required = false) String orderId,
            @Parameter(description = "商家ID") @RequestParam(required = false) String merchantId,
            @Parameter(description = "协调员ID") @RequestParam(required = false) String coordinatorId,
            @Parameter(description = "片区长ID") @RequestParam(required = false) String sectorLeaderId,
            @Parameter(description = "楼栋长ID") @RequestParam(required = false) String individualLeaderId,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        return ApiResponse.success(distributionService.listRecords(
                page, pageSize, orderId, merchantId, coordinatorId, sectorLeaderId,
                individualLeaderId, propertyCompanyId, startDate, endDate));
    }

    @Operation(summary = "管理端分成统计", description = "分成统计（管理端路径，对齐 docs/03 16.12）")
    @GetMapping("/admin/distribution/statistics")
    public ApiResponse<DistributionStatsResponse> adminGetStats(
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "统计维度", example = "summary") @RequestParam(defaultValue = "summary") String dimension,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(distributionService.getStats(startDate, endDate, dimension, propertyCompanyId));
    }
}
