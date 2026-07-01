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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/point-clear")
@RequiredArgsConstructor
public class AdminPointClearController {

    private final AdminPointClearService adminPointClearService;

    @PostMapping("/run")
    public ApiResponse<RunPointClearResponse> run(@Valid @RequestBody RunPointClearRequest request) {
        return ApiResponse.success(adminPointClearService.run(request));
    }

    @GetMapping("/records")
    public ApiResponse<PageResponse<PointClearRecordItemResponse>> listRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String residentId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminPointClearService.listRecords(
                page, pageSize, residentId, startDate, endDate, propertyCompanyId));
    }
}
