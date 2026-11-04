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

    @Operation(summary = "我的分成记录",
            description = "按当前身份自动收敛：商家/组长/技工看店铺，统筹/板块/个体/楼长看本人挂接记录，只返回本人份额。Auth: Required")
    @GetMapping("/distribution/records/mine")
    public ApiResponse<PageResponse<com.wuye.points.dto.distribution.MyDistributionRecordItemResponse>> listMyRecords(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(distributionService.listMyRecords(page, pageSize));
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

    @Operation(summary = "分成试算预览", description = "试算分成（预览，不落库）。支持 orderId 快照试算 / merchantId 假设试算两种模式")
    @GetMapping("/distribution/calculate")
    public ApiResponse<DistributionCalculateResponse> calculatePreview(
            @Parameter(description = "订单ID（传入时读订单快照，忽略其它金额参数）") @RequestParam(required = false) String orderId,
            @Parameter(description = "商家ID") @RequestParam(required = false) String merchantId,
            @Parameter(description = "商品金额（不含配送费）") @RequestParam(required = false) BigDecimal productAmount,
            @Parameter(description = "订单总金额（兼容旧调用 = 商品金额 + 配送费）") @RequestParam(required = false) BigDecimal totalAmount,
            @Parameter(description = "满额减免前基础配送费") @RequestParam(required = false) BigDecimal originalDeliveryFee,
            @Parameter(description = "实付配送费") @RequestParam(required = false) BigDecimal deliveryFee,
            @Parameter(description = "减免承担方 none/merchant/property/platform") @RequestParam(required = false) String deliverySubsidySponsor,
            @Parameter(description = "使用积分数") @RequestParam(required = false) Integer pointUsed,
            @Parameter(description = "使用物业币数") @RequestParam(required = false) BigDecimal coinUsed,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(distributionService.calculatePreview(
                orderId, merchantId, productAmount, totalAmount, originalDeliveryFee,
                deliveryFee, deliverySubsidySponsor, pointUsed, coinUsed, propertyCompanyId));
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
