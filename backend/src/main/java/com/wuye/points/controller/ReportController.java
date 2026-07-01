package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.distribution.DistributionStatsResponse;
import com.wuye.points.dto.report.ReportOrderSummaryResponse;
import com.wuye.points.dto.report.ReportOverviewResponse;
import com.wuye.points.dto.report.ReportPointSummaryResponse;
import com.wuye.points.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;

/**
 * 报表模块
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 总览报表
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @GetMapping("/overview")
    public ApiResponse<ReportOverviewResponse> getOverview(
            @RequestParam(required = false) String propertyCompanyId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ApiResponse.success(reportService.getOverview(propertyCompanyId, startDate, endDate));
    }

    /**
     * 积分汇总
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @GetMapping("/point-summary")
    public ApiResponse<ReportPointSummaryResponse> getPointSummary(
            @RequestParam(required = false) String propertyCompanyId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String groupBy) {
        return ApiResponse.success(reportService.getPointSummary(
                propertyCompanyId, startDate, endDate, groupBy));
    }

    /**
     * 订单汇总
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @GetMapping("/order-summary")
    public ApiResponse<ReportOrderSummaryResponse> getOrderSummary(
            @RequestParam(required = false) String propertyCompanyId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String groupBy) {
        return ApiResponse.success(reportService.getOrderSummary(
                propertyCompanyId, startDate, endDate, groupBy));
    }

    /**
     * 分成汇总
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @GetMapping("/distribution-summary")
    public ApiResponse<DistributionStatsResponse> getDistributionSummary(
            @RequestParam(required = false) String propertyCompanyId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "summary") String dimension) {
        return ApiResponse.success(reportService.getDistributionSummary(
                propertyCompanyId, startDate, endDate, dimension));
    }

    /**
     * 导出报表（CSV 流式返回）
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @GetMapping("/export")
    public ResponseEntity<StreamingResponseBody> exportReport(
            @RequestParam String type,
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String propertyCompanyId) {
        String filename = reportService.resolveExportFilename(type, month, startDate, endDate, propertyCompanyId);
        StreamingResponseBody body = reportService.exportReport(
                type, format, month, startDate, endDate, propertyCompanyId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }
}
