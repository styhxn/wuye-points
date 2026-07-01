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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/point-pools")
@RequiredArgsConstructor
public class AdminPointPoolController {

    private final AdminPointPoolService adminPointPoolService;

    @GetMapping
    public ApiResponse<PointPoolBalanceResponse> getBalance(
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminPointPoolService.getBalance(propertyCompanyId));
    }

    @GetMapping("/records")
    public ApiResponse<PageResponse<PointPoolRecordItemResponse>> listRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String poolType,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminPointPoolService.listRecords(
                page, pageSize, poolType, source, startDate, endDate, propertyCompanyId));
    }

    @PostMapping("/adjust")
    public ApiResponse<AdjustPointPoolResponse> adjust(
            @Valid @RequestBody AdjustPointPoolRequest request) {
        return ApiResponse.success(adminPointPoolService.adjust(request));
    }
}
