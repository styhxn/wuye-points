package com.wuye.points.controller;

import com.wuye.points.dto.admin.pointpool.AdjustPointPoolRequest;
import com.wuye.points.dto.admin.pointpool.AdjustPointPoolResponse;
import com.wuye.points.dto.admin.pointpool.PointPoolBalanceResponse;
import com.wuye.points.dto.admin.pointpool.PointPoolRecordItemResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminPointPoolService;
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

@Tag(name = "管理端 — 积分池管理", description = "积分池余额、记录列表、调整积分池")
@RestController
@RequestMapping("/admin/point-pools")
@RequiredArgsConstructor
public class AdminPointPoolController {

    private final AdminPointPoolService adminPointPoolService;

    @Operation(summary = "积分池余额", description = "获取积分池余额信息；物业尚无积分池时返回 0 值概览（不报 404）。角色: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<PointPoolBalanceResponse> getBalance(
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminPointPoolService.getBalance(propertyCompanyId));
    }

    @Operation(summary = "积分池记录列表", description = "获取积分池记录列表。角色: property_admin, platform_admin")
    @GetMapping("/records")
    public ApiResponse<PageResponse<PointPoolRecordItemResponse>> listRecords(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "积分池类型") @RequestParam(required = false) String poolType,
            @Parameter(description = "来源") @RequestParam(required = false) String source,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminPointPoolService.listRecords(
                page, pageSize, poolType, source, startDate, endDate, propertyCompanyId));
    }

    @Operation(summary = "调整积分池", description = "调整积分池余额。角色: property_admin, platform_admin")
    @PostMapping("/adjust")
    public ApiResponse<AdjustPointPoolResponse> adjust(
            @Valid @RequestBody AdjustPointPoolRequest request) {
        return ApiResponse.success(adminPointPoolService.adjust(request));
    }
}
