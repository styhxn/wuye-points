package com.wuye.points.controller;

import com.wuye.points.dto.admin.operationlog.OperationLogItemResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminOperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/operation-logs")
@RequiredArgsConstructor
public class AdminOperationLogController {

    private final AdminOperationLogService adminOperationLogService;

    @GetMapping
    public ApiResponse<PageResponse<OperationLogItemResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String operatorId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String targetId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminOperationLogService.list(
                page, pageSize, operatorId, module, action, targetType, targetId,
                startDate, endDate, propertyCompanyId));
    }
}
