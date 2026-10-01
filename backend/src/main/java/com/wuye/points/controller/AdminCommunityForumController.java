package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.community.CommunityCommentResponse;
import com.wuye.points.dto.community.CommunityPostResponse;
import com.wuye.points.dto.community.ContentReportResponse;
import com.wuye.points.dto.community.HandleContentReportRequest;
import com.wuye.points.dto.community.SetPostVisibilityRequest;
import com.wuye.points.service.CommunityForumService;
import com.wuye.points.service.ContentReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/community")
@Tag(name = "社区论坛管理", description = "管理端：帖子/评论删除、举报处理（按物业公司管辖范围）")
@RequiredArgsConstructor
public class AdminCommunityForumController {

    private final CommunityForumService communityForumService;
    private final ContentReportService contentReportService;

    @Operation(summary = "管理端帖子列表", description = "含隐藏/已删除；status 可选 published/hidden/deleted")
    @GetMapping("/posts")
    public ApiResponse<PageResponse<CommunityPostResponse>> listPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(PageResponse.from(communityForumService.adminListPosts(page, pageSize, status, keyword)));
    }

    @Operation(summary = "管理端帖子下的评论列表", description = "含已删除评论；status 可选 published/deleted 或不传返回全部")
    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<PageResponse<CommunityCommentResponse>> listComments(
            @PathVariable String postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(PageResponse.from(
                communityForumService.adminListComments(postId, page, pageSize, status)));
    }

    @Operation(summary = "管理端删帖")
    @DeleteMapping("/posts/{id}")
    public ApiResponse<Void> deletePost(@PathVariable String id) {
        communityForumService.adminDeletePost(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "管理端删评论")
    @DeleteMapping("/comments/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable String id) {
        communityForumService.adminDeleteComment(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "设置帖子可见范围", description = "visibilityType=community 本小区可见；=family 仅指定住户可见。角色: property_admin")
    @PostMapping("/posts/{id}/visibility")
    public ApiResponse<Void> setPostVisibility(
            @PathVariable String id, @Valid @RequestBody SetPostVisibilityRequest req) {
        communityForumService.setPostVisibility(id, req.getVisibilityType(), req.getVisibleResidentId());
        return ApiResponse.success(null);
    }

    @Operation(summary = "举报列表", description = "status 可选 pending/accepted/rejected")
    @GetMapping("/reports")
    public ApiResponse<PageResponse<ContentReportResponse>> listReports(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(PageResponse.from(contentReportService.listReports(page, pageSize, status)));
    }

    @Operation(summary = "处理举报", description = "action=accept 通过举报并自动隐藏内容；action=reject 驳回")
    @PostMapping("/reports/{id}/handle")
    public ApiResponse<ContentReportResponse> handleReport(
            @PathVariable String id, @Valid @RequestBody HandleContentReportRequest req) {
        return ApiResponse.success(contentReportService.handleReport(id, req));
    }
}
