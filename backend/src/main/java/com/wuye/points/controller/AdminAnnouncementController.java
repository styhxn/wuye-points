package com.wuye.points.controller;

import com.wuye.points.dto.announcement.AnnouncementCollectListResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公告管理端接口
 */
@RestController
@RequestMapping("/admin/announcements")
@RequiredArgsConstructor
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping("/{id}/collects")
    public ApiResponse<AnnouncementCollectListResponse> listCollects(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(announcementService.listCollects(id, page, pageSize, keyword));
    }
}
