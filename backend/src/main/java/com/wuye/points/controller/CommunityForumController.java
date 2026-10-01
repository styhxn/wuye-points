package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.community.CommunityCommentResponse;
import com.wuye.points.dto.community.CommunityPostResponse;
import com.wuye.points.dto.community.ContentReportResponse;
import com.wuye.points.dto.community.CreateCommunityCommentRequest;
import com.wuye.points.dto.community.CreateCommunityPostRequest;
import com.wuye.points.dto.community.CreateContentReportRequest;
import com.wuye.points.dto.community.ToggleLikeResponse;
import com.wuye.points.service.CommunityForumService;
import com.wuye.points.service.ContentReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/community")
@Tag(name = "社区论坛", description = "小区动态：发帖 / 帖子流 / 详情 / 点赞 / 评论（支持二级回复）/ 举报（仅本小区可见）")
@RequiredArgsConstructor
@Validated
public class CommunityForumController {

    private final CommunityForumService communityForumService;
    private final ContentReportService contentReportService;

    @Operation(summary = "发帖")
    @PostMapping("/posts")
    public ApiResponse<CommunityPostResponse> createPost(@Valid @RequestBody CreateCommunityPostRequest req) {
        return ApiResponse.success(communityForumService.createPost(req));
    }

    @Operation(summary = "本小区帖子流", description = "sort=newest(默认)/hot/oldest；keyword 按内容模糊搜索")
    @GetMapping("/posts")
    public ApiResponse<PageResponse<CommunityPostResponse>> listPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(PageResponse.from(communityForumService.listPosts(page, pageSize, sort, keyword)));
    }

    @Operation(summary = "帖子详情")
    @GetMapping("/posts/{id}")
    public ApiResponse<CommunityPostResponse> getPost(@PathVariable String id) {
        return ApiResponse.success(communityForumService.getPost(id));
    }

    @Operation(summary = "删除自己的帖子")
    @DeleteMapping("/posts/{id}")
    public ApiResponse<Void> deletePost(@PathVariable String id) {
        communityForumService.deleteOwnPost(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "点赞 / 取消点赞")
    @PostMapping("/posts/{id}/like")
    public ApiResponse<ToggleLikeResponse> toggleLike(@PathVariable String id) {
        return ApiResponse.success(communityForumService.toggleLike(id));
    }

    @Operation(summary = "评论（支持二级回复）", description = "回复某条评论时填写 parentId 与 replyToUserId/replyToUserName")
    @PostMapping("/posts/{id}/comments")
    public ApiResponse<CommunityCommentResponse> comment(
            @PathVariable String id, @Valid @RequestBody CreateCommunityCommentRequest req) {
        return ApiResponse.success(communityForumService.commentOnPost(id, req));
    }

    @Operation(summary = "评论列表", description = "按发布时间升序，前端按 parentId 组装二级回复")
    @GetMapping("/posts/{id}/comments")
    public ApiResponse<PageResponse<CommunityCommentResponse>> listComments(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        return ApiResponse.success(PageResponse.from(communityForumService.listComments(id, page, pageSize)));
    }

    @Operation(summary = "删除自己的评论")
    @DeleteMapping("/comments/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable String id) {
        communityForumService.deleteOwnComment(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "举报帖子或评论")
    @PostMapping("/reports")
    public ApiResponse<ContentReportResponse> createReport(@Valid @RequestBody CreateContentReportRequest req) {
        return ApiResponse.success(contentReportService.createReport(req));
    }
}
