package com.wuye.points.controller;

import com.wuye.points.dto.announcement.AnnouncementCollectListResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公告管理端接口
 */
@Tag(name = "管理端 — 公告管理", description = "公告收集列表")
@RestController
@RequestMapping("/admin/announcements")
@RequiredArgsConstructor
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "公告信息收集列表", description = "获取公告信息收集列表。角色: property_admin, platform_admin")
    @GetMapping("/{id}/collects")
    public ApiResponse<AnnouncementCollectListResponse> listCollects(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return ApiResponse.success(announcementService.listCollects(id, page, pageSize, keyword));
    }
}
