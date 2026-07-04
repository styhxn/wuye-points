package com.wuye.points.controller;

import com.wuye.points.dto.admin.offlinepoint.GrantOfflinePointRequest;
import com.wuye.points.dto.admin.offlinepoint.GrantOfflinePointResponse;
import com.wuye.points.dto.admin.offlinepoint.OfflinePointRecordItemResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminOfflinePointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端 — 线下积分发放", description = "线下发放积分、发放记录")
@RestController
@RequestMapping("/admin/offline-points")
@RequiredArgsConstructor
public class AdminOfflinePointController {

    private final AdminOfflinePointService adminOfflinePointService;

    @Operation(summary = "线下发放积分", description = "线下发放积分。角色: property_admin, platform_admin")
    @PostMapping("/grant")
    public ApiResponse<GrantOfflinePointResponse> grant(
            @Valid @RequestBody GrantOfflinePointRequest request) {
        return ApiResponse.success(adminOfflinePointService.grant(request));
    }

    @Operation(summary = "发放记录列表", description = "获取线下积分发放记录列表。角色: property_admin, platform_admin")
    @GetMapping("/records")
    public ApiResponse<PageResponse<OfflinePointRecordItemResponse>> listRecords(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "业主ID") @RequestParam(required = false) String residentId,
            @Parameter(description = "操作者ID") @RequestParam(required = false) String operatorId,
            @Parameter(description = "来源") @RequestParam(required = false) String source,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminOfflinePointService.listRecords(
                page, pageSize, residentId, operatorId, source, startDate, endDate, propertyCompanyId));
    }
}
