package com.wuye.points.controller;

import com.wuye.points.dto.announcement.AnnouncementCollectListResponse;
import com.wuye.points.dto.announcement.AnnouncementDetailResponse;
import com.wuye.points.dto.announcement.AnnouncementListItemResponse;
import com.wuye.points.dto.announcement.CreateAnnouncementRequest;
import com.wuye.points.dto.announcement.DeleteAnnouncementResponse;
import com.wuye.points.dto.announcement.SubmitCollectRequest;
import com.wuye.points.dto.announcement.SubmitCollectResponse;
import com.wuye.points.dto.announcement.UpdateAnnouncementRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AnnouncementService;
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
 * 公告模块
 */
@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping
    public ApiResponse<AnnouncementDetailResponse> createAnnouncement(
            @Valid @RequestBody CreateAnnouncementRequest request) {
        return ApiResponse.success(announcementService.createAnnouncement(request));
    }

    @GetMapping
    public ApiResponse<PageResponse<AnnouncementListItemResponse>> listAnnouncements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String announcementType,
            @RequestParam(required = false) String communityId,
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) String readStatus,
            @RequestParam(defaultValue = "-publishedAt") String sort) {
        return ApiResponse.success(announcementService.listAnnouncements(
                page, pageSize, announcementType, communityId, merchantId, readStatus, sort));
    }

    @GetMapping("/{id}")
    public ApiResponse<AnnouncementDetailResponse> getAnnouncement(@PathVariable String id) {
        return ApiResponse.success(announcementService.getAnnouncement(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<AnnouncementDetailResponse> updateAnnouncement(
            @PathVariable String id,
            @Valid @RequestBody UpdateAnnouncementRequest request) {
        return ApiResponse.success(announcementService.updateAnnouncement(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<DeleteAnnouncementResponse> deleteAnnouncement(@PathVariable String id) {
        return ApiResponse.success(announcementService.deleteAnnouncement(id));
    }

    @PostMapping("/{id}/collect")
    public ApiResponse<SubmitCollectResponse> submitCollect(
            @PathVariable String id,
            @Valid @RequestBody SubmitCollectRequest request) {
        return ApiResponse.success(announcementService.submitCollect(id, request));
    }
}
