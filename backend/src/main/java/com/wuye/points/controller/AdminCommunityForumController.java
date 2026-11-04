package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.community.CommunityCommentResponse;
import com.wuye.points.dto.community.CommunityPostResponse;
import com.wuye.points.dto.community.ContentReportResponse;
import com.wuye.points.dto.community.ForumBanRequest;
import com.wuye.points.dto.community.HandleContentReportRequest;
import com.wuye.points.dto.community.ResidentForumBanItemResponse;
import com.wuye.points.dto.community.SetPostVisibilityRequest;
import com.wuye.points.service.CommunityForumService;
import com.wuye.points.service.ContentReportService;
import com.wuye.points.service.ForumBanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    private final ForumBanService forumBanService;

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

    @Operation(summary = "设置帖子可见范围", description = "visibilityType=community 本小区可见；=family 仅指定住户可见。权限: community:manage")
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

    // ============== 论坛禁言（影子封禁）==============

    @Operation(summary = "禁言用户（影子封禁）",
            description = "禁止某住户发帖/评论。被禁用户无感知：发帖/评论照常成功且本人可见，其他住户不可见。"
                    + "expiresAt 不传=永久禁言。权限: community:manage（限本物业公司）")
    @PostMapping("/users/{residentId}/forum-ban")
    public ApiResponse<ResidentForumBanItemResponse> banUser(
            @Parameter(description = "被禁住户ID") @PathVariable String residentId,
            @Valid @RequestBody ForumBanRequest req) {
        return ApiResponse.success(forumBanService.ban(residentId, req));
    }

    @Operation(summary = "解除用户禁言",
            description = "解除禁言并恢复该用户历史内容可见、回补评论计数。权限: community:manage（限本物业公司）")
    @DeleteMapping("/users/{residentId}/forum-ban")
    public ApiResponse<ResidentForumBanItemResponse> unbanUser(
            @Parameter(description = "被禁住户ID") @PathVariable String residentId) {
        return ApiResponse.success(forumBanService.unban(residentId));
    }

    @Operation(summary = "禁言列表",
            description = "禁言记录列表（可按状态过滤 active/lifted/expired）。权限: community:read（限本物业公司）")
    @GetMapping("/forum-bans")
    public ApiResponse<PageResponse<ResidentForumBanItemResponse>> listForumBans(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(forumBanService.listBans(page, pageSize, status));
    }
}
