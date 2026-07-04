package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.distribution.DistributionStatsResponse;
import com.wuye.points.dto.report.ReportOrderSummaryResponse;
import com.wuye.points.dto.report.ReportOverviewResponse;
import com.wuye.points.dto.report.ReportPointSummaryResponse;
import com.wuye.points.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;

/**
 * 报表模块
 */
@Tag(name = "报表模块", description = "总览报表、积分汇总、订单汇总、分成汇总、导出")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "总览报表", description = "总览报表。Auth: Required | Roles: property_admin, platform_admin")
    @GetMapping("/overview")
    public ApiResponse<ReportOverviewResponse> getOverview(
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        return ApiResponse.success(reportService.getOverview(propertyCompanyId, startDate, endDate));
    }

    @Operation(summary = "积分汇总报表", description = "积分汇总。Auth: Required | Roles: property_admin, platform_admin")
    @GetMapping("/point-summary")
    public ApiResponse<ReportPointSummaryResponse> getPointSummary(
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "分组维度") @RequestParam(required = false) String groupBy) {
        return ApiResponse.success(reportService.getPointSummary(
                propertyCompanyId, startDate, endDate, groupBy));
    }

    @Operation(summary = "订单汇总报表", description = "订单汇总。Auth: Required | Roles: property_admin, platform_admin")
    @GetMapping("/order-summary")
    public ApiResponse<ReportOrderSummaryResponse> getOrderSummary(
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "分组维度") @RequestParam(required = false) String groupBy) {
        return ApiResponse.success(reportService.getOrderSummary(
                propertyCompanyId, startDate, endDate, groupBy));
    }

    @Operation(summary = "分成汇总报表", description = "分成汇总。Auth: Required | Roles: property_admin, platform_admin")
    @GetMapping("/distribution-summary")
    public ApiResponse<DistributionStatsResponse> getDistributionSummary(
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "统计维度", example = "summary") @RequestParam(defaultValue = "summary") String dimension) {
        return ApiResponse.success(reportService.getDistributionSummary(
                propertyCompanyId, startDate, endDate, dimension));
    }

    @Operation(summary = "导出报表", description = "导出报表（CSV 流式返回）。Auth: Required | Roles: property_admin, platform_admin")
    @GetMapping("/export")
    public ResponseEntity<StreamingResponseBody> exportReport(
            @Parameter(description = "报表类型") @RequestParam String type,
            @Parameter(description = "导出格式", example = "csv") @RequestParam(defaultValue = "csv") String format,
            @Parameter(description = "月份") @RequestParam(required = false) String month,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        String filename = reportService.resolveExportFilename(type, month, startDate, endDate, propertyCompanyId);
        StreamingResponseBody body = reportService.exportReport(
                type, format, month, startDate, endDate, propertyCompanyId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }
}
