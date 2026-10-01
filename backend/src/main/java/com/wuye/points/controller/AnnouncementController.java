package com.wuye.points.controller;

import com.wuye.points.dto.announcement.AnnouncementCollectListResponse;
import com.wuye.points.dto.announcement.AnnouncementDetailResponse;
import com.wuye.points.dto.announcement.AnnouncementListItemResponse;
import com.wuye.points.dto.announcement.ActivityAnnouncementRequest;
import com.wuye.points.dto.announcement.CreateAnnouncementRequest;
import com.wuye.points.dto.announcement.DeleteAnnouncementResponse;
import com.wuye.points.dto.announcement.SubmitCollectRequest;
import com.wuye.points.dto.announcement.SubmitCollectResponse;
import com.wuye.points.dto.announcement.UpdateAnnouncementRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "公告模块", description = "公告创建、列表、详情、更新、删除、信息收集")
@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "创建公告", description = "创建公告")
    @PostMapping
    public ApiResponse<AnnouncementDetailResponse> createAnnouncement(
            @Valid @RequestBody CreateAnnouncementRequest request) {
        return ApiResponse.success(announcementService.createAnnouncement(request));
    }

    @Operation(summary = "发布活动公告（移动端活动组长）", description = "发布活动类公告，角色：activity_leader/property_admin/platform_admin；公告类型强制为 activity。无需传 announcementType")
    @PostMapping("/activity")
    public ApiResponse<AnnouncementDetailResponse> createActivityAnnouncement(
            @Valid @RequestBody ActivityAnnouncementRequest request) {
        return ApiResponse.success(announcementService.createActivityAnnouncement(request));
    }

    @Operation(summary = "公告列表", description = "分页查询公告列表")
    @GetMapping
    public ApiResponse<PageResponse<AnnouncementListItemResponse>> listAnnouncements(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "公告类型") @RequestParam(required = false) String announcementType,
            @Parameter(description = "小区ID") @RequestParam(required = false) String communityId,
            @Parameter(description = "商家ID") @RequestParam(required = false) String merchantId,
            @Parameter(description = "已读状态") @RequestParam(required = false) String readStatus,
            @Parameter(description = "排序字段", example = "-publishedAt") @RequestParam(defaultValue = "-publishedAt") String sort) {
        return ApiResponse.success(announcementService.listAnnouncements(
                page, pageSize, announcementType, communityId, merchantId, readStatus, sort));
    }

    @Operation(summary = "公告详情", description = "获取公告详情")
    @GetMapping("/{id}")
    public ApiResponse<AnnouncementDetailResponse> getAnnouncement(
            @Parameter(description = "公告ID", example = "1") @PathVariable String id) {
        return ApiResponse.success(announcementService.getAnnouncement(id));
    }

    @Operation(summary = "更新公告", description = "更新公告")
    @PutMapping("/{id}")
    public ApiResponse<AnnouncementDetailResponse> updateAnnouncement(
            @Parameter(description = "公告ID", example = "1") @PathVariable String id,
            @Valid @RequestBody UpdateAnnouncementRequest request) {
        return ApiResponse.success(announcementService.updateAnnouncement(id, request));
    }

    @Operation(summary = "删除公告", description = "删除公告")
    @DeleteMapping("/{id}")
    public ApiResponse<DeleteAnnouncementResponse> deleteAnnouncement(
            @Parameter(description = "公告ID", example = "1") @PathVariable String id) {
        return ApiResponse.success(announcementService.deleteAnnouncement(id));
    }

    @Operation(summary = "提交信息收集", description = "提交信息收集")
    @PostMapping("/{id}/collect")
    public ApiResponse<SubmitCollectResponse> submitCollect(
            @Parameter(description = "公告ID", example = "1") @PathVariable String id,
            @Valid @RequestBody SubmitCollectRequest request) {
        return ApiResponse.success(announcementService.submitCollect(id, request));
    }
}
