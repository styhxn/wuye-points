package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.propertyfee.ArrearsReminderPreviewResponse;
import com.wuye.points.dto.propertyfee.ArrearsReminderRequest;
import com.wuye.points.dto.propertyfee.ArrearsReminderResponse;
import com.wuye.points.dto.propertyfeereport.ArrearsReportResponse;
import com.wuye.points.service.PropertyFeeReportService;
import com.wuye.points.service.PropertyFeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * 管理端 — 物业费报表（欠费报表）
 *
 * <p>独立控制器，路由前缀 /admin/property-fees 对齐 API 文档 §83。
 */
@Tag(name = "管理端 — 物业费报表", description = "欠费报表查询与导出")
@RestController
@RequestMapping("/admin/property-fees")
@RequiredArgsConstructor
public class AdminPropertyFeeController {

    private final PropertyFeeReportService propertyFeeReportService;
    private final PropertyFeeService propertyFeeService;

    @Operation(summary = "欠费报表",
            description = "查询欠费报表（JSON）。Auth: Required | Roles: property_admin, platform_admin")
    @GetMapping("/arrears-report")
    public ApiResponse<ArrearsReportResponse> arrearsReport(
            @Parameter(description = "物业公司ID（property_admin 自动限定本公司）")
            @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "社区ID") @RequestParam(required = false) String communityId,
            @Parameter(description = "楼栋") @RequestParam(required = false) String building,
            @Parameter(description = "费用类型：property/parking/water/electricity")
            @RequestParam(required = false) String feeType) {
        return ApiResponse.success(propertyFeeReportService.generateArrearsReport(
                propertyCompanyId, communityId, building, feeType));
    }

    @Operation(summary = "导出欠费报表",
            description = "导出欠费报表（CSV）。Auth: Required | Roles: property_admin, platform_admin")
    @GetMapping("/arrears-report/export")
    public ResponseEntity<byte[]> exportArrearsReport(
            @Parameter(description = "物业公司ID（property_admin 自动限定本公司）")
            @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "社区ID") @RequestParam(required = false) String communityId,
            @Parameter(description = "楼栋") @RequestParam(required = false) String building,
            @Parameter(description = "费用类型：property/parking/water/electricity")
            @RequestParam(required = false) String feeType) {
        String csv = propertyFeeReportService.exportArrearsCsv(
                propertyCompanyId, communityId, building, feeType);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        String filename = "arrears-report.csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    @Operation(summary = "催缴通知（统一发送）", description = "依据预览快照发送催缴消息，按渠道拆分统计。需先调用预览接口获取 previewToken 并原样回传；requestId 幂等。Auth: Required | Roles: property_admin, platform_admin")
    @PostMapping("/arrears-reminder")
    public ApiResponse<ArrearsReminderResponse> sendArrearsReminder(
            @Valid @RequestBody ArrearsReminderRequest request) {
        return ApiResponse.success(propertyFeeService.sendArrearsReminder(request));
    }

    @Operation(summary = "催缴人数预览",
            description = "发送前预览欠费范围与金额，返回 previewToken（30 分钟有效）。Auth: Required | Roles: property_admin, platform_admin。注：规范写为 GET，因需请求体此处用 POST。")
    @PostMapping("/arrears-reminder/preview")
    public ApiResponse<ArrearsReminderPreviewResponse> previewArrearsReminder(
            @RequestBody ArrearsReminderRequest request) {
        return ApiResponse.success(propertyFeeService.previewArrearsReminder(request));
    }
}
