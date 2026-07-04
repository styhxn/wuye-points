package com.wuye.points.controller;

import com.wuye.points.dto.admin.operationlog.OperationLogItemResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminOperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端 — 操作日志", description = "操作日志列表")
@RestController
@RequestMapping("/admin/operation-logs")
@RequiredArgsConstructor
public class AdminOperationLogController {

    private final AdminOperationLogService adminOperationLogService;

    @Operation(summary = "操作日志列表", description = "获取操作日志列表。角色: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<PageResponse<OperationLogItemResponse>> list(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "操作者ID") @RequestParam(required = false) String operatorId,
            @Parameter(description = "模块") @RequestParam(required = false) String module,
            @Parameter(description = "操作类型") @RequestParam(required = false) String action,
            @Parameter(description = "目标类型") @RequestParam(required = false) String targetType,
            @Parameter(description = "目标ID") @RequestParam(required = false) String targetId,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminOperationLogService.list(
                page, pageSize, operatorId, module, action, targetType, targetId,
                startDate, endDate, propertyCompanyId));
    }
}
