package com.wuye.points.controller;

import com.wuye.points.dto.admin.sectorleader.AppointSectorLeaderRequest;
import com.wuye.points.dto.admin.sectorleader.SectorLeaderDetailResponse;
import com.wuye.points.dto.admin.sectorleader.SectorLeaderListItemResponse;
import com.wuye.points.dto.admin.sectorleader.UpdateSectorLeaderRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminSectorLeaderService;
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
 * 板块负责人管理模块（管理端）
 */
@RestController
@RequestMapping("/admin/sector-leaders")
@RequiredArgsConstructor
public class AdminSectorLeaderController {

    private final AdminSectorLeaderService adminSectorLeaderService;

    @GetMapping
    public ApiResponse<PageResponse<SectorLeaderListItemResponse>> listSectorLeaders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String coordinatorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminSectorLeaderService.listSectorLeaders(
                page, pageSize, sector, coordinatorId, status, propertyCompanyId));
    }

    @PostMapping
    public ApiResponse<SectorLeaderDetailResponse> appointSectorLeader(
            @Valid @RequestBody AppointSectorLeaderRequest request) {
        return ApiResponse.success(adminSectorLeaderService.appointSectorLeader(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<SectorLeaderDetailResponse> getSectorLeader(@PathVariable String id) {
        return ApiResponse.success(adminSectorLeaderService.getSectorLeader(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<SectorLeaderDetailResponse> updateSectorLeader(
            @PathVariable String id,
            @Valid @RequestBody UpdateSectorLeaderRequest request) {
        return ApiResponse.success(adminSectorLeaderService.updateSectorLeader(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<SectorLeaderDetailResponse> revokeSectorLeader(
            @PathVariable String id,
            @RequestParam String reason) {
        return ApiResponse.success(adminSectorLeaderService.revokeSectorLeader(id, reason));
    }
}
