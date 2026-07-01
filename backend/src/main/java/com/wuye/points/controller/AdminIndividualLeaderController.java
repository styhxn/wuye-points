package com.wuye.points.controller;

import com.wuye.points.dto.admin.individualleader.AppointIndividualLeaderRequest;
import com.wuye.points.dto.admin.individualleader.IndividualLeaderDetailResponse;
import com.wuye.points.dto.admin.individualleader.IndividualLeaderListItemResponse;
import com.wuye.points.dto.admin.individualleader.UpdateIndividualLeaderRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminIndividualLeaderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个体负责人管理模块（管理端）
 */
@RestController
@RequestMapping("/admin/individual-leaders")
@RequiredArgsConstructor
public class AdminIndividualLeaderController {

    private final AdminIndividualLeaderService adminIndividualLeaderService;

    @GetMapping
    public ApiResponse<PageResponse<IndividualLeaderListItemResponse>> listIndividualLeaders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String sectorLeaderId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminIndividualLeaderService.listIndividualLeaders(
                page, pageSize, sectorLeaderId, keyword, status, sector, propertyCompanyId));
    }

    @PostMapping
    public ApiResponse<IndividualLeaderDetailResponse> appointIndividualLeader(
            @Valid @RequestBody AppointIndividualLeaderRequest request) {
        return ApiResponse.success(adminIndividualLeaderService.appointIndividualLeader(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<IndividualLeaderDetailResponse> getIndividualLeader(@PathVariable String id) {
        return ApiResponse.success(adminIndividualLeaderService.getIndividualLeader(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<IndividualLeaderDetailResponse> updateIndividualLeader(
            @PathVariable String id,
            @Valid @RequestBody UpdateIndividualLeaderRequest request) {
        return ApiResponse.success(adminIndividualLeaderService.updateIndividualLeader(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<IndividualLeaderDetailResponse> revokeIndividualLeader(
            @PathVariable String id,
            @RequestParam String reason) {
        return ApiResponse.success(adminIndividualLeaderService.revokeIndividualLeader(id, reason));
    }
}
