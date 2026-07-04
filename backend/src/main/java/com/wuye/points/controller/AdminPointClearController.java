package com.wuye.points.controller;

import com.wuye.points.dto.admin.pointclear.PointClearRecordItemResponse;
import com.wuye.points.dto.admin.pointclear.RunPointClearRequest;
import com.wuye.points.dto.admin.pointclear.RunPointClearResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminPointClearService;
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

@Tag(name = "管理端 — 积分清零", description = "执行积分清零、清零记录")
@RestController
@RequestMapping("/admin/point-clear")
@RequiredArgsConstructor
public class AdminPointClearController {

    private final AdminPointClearService adminPointClearService;

    @Operation(summary = "执行积分清零", description = "执行积分清零操作。角色: property_admin, platform_admin")
    @PostMapping("/run")
    public ApiResponse<RunPointClearResponse> run(@Valid @RequestBody RunPointClearRequest request) {
        return ApiResponse.success(adminPointClearService.run(request));
    }

    @Operation(summary = "清零记录列表", description = "获取积分清零记录列表。角色: property_admin, platform_admin")
    @GetMapping("/records")
    public ApiResponse<PageResponse<PointClearRecordItemResponse>> listRecords(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "业主ID") @RequestParam(required = false) String residentId,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminPointClearService.listRecords(
                page, pageSize, residentId, startDate, endDate, propertyCompanyId));
    }
}
