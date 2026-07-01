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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/offline-points")
@RequiredArgsConstructor
public class AdminOfflinePointController {

    private final AdminOfflinePointService adminOfflinePointService;

    @PostMapping("/grant")
    public ApiResponse<GrantOfflinePointResponse> grant(
            @Valid @RequestBody GrantOfflinePointRequest request) {
        return ApiResponse.success(adminOfflinePointService.grant(request));
    }

    @GetMapping("/records")
    public ApiResponse<PageResponse<OfflinePointRecordItemResponse>> listRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String residentId,
            @RequestParam(required = false) String operatorId,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminOfflinePointService.listRecords(
                page, pageSize, residentId, operatorId, source, startDate, endDate, propertyCompanyId));
    }
}
