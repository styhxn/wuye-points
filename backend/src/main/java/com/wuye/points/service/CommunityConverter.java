package com.wuye.points.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.dto.community.CommunityCommentResponse;
import com.wuye.points.dto.community.CommunityPostResponse;
import com.wuye.points.dto.community.ContentReportResponse;
import com.wuye.points.entity.CommunityComment;
import com.wuye.points.entity.CommunityPost;
import com.wuye.points.entity.ContentReport;
import com.wuye.points.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CommunityConverter {

    private final ObjectMapper objectMapper;

    /**
     * 帖子响应转换。
     *
     * @param likedByMe   当前用户是否已点赞
     * @param adminView   管理端视图：为 true 时才回显 shadowBanned 影子封禁标记。
     *                    C 端（住户）一律不返回该标记——被禁言用户应无感知，
     *                    不能让其从响应中得知自己已被封禁。
     */
    public CommunityPostResponse toPostResponse(CommunityPost post, boolean likedByMe, boolean adminView) {
        return CommunityPostResponse.builder()
                .id(post.getId())
                .authorId(post.getAuthorId())
                .authorName(post.getAuthorName())
                .authorRole(post.getAuthorRole())
                .content(post.getContent())
                .imageUrls(parseList(post.getImageUrls()))
                .status(post.getStatus())
                .likeCount(post.getLikeCount() == null ? 0 : post.getLikeCount())
                .commentCount(post.getCommentCount() == null ? 0 : post.getCommentCount())
                .likedByMe(likedByMe)
                .shadowBanned(adminView ? Boolean.TRUE.equals(post.getShadowBanned()) : null)
                .createdAt(DateTimeUtil.toIsoUtc(post.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(post.getUpdatedAt()))
                .build();
    }

    /** C 端视图（不回显影子封禁标记） */
    public CommunityPostResponse toPostResponse(CommunityPost post, boolean likedByMe) {
        return toPostResponse(post, likedByMe, false);
    }

    /** 管理端视图（回显影子封禁标记） */
    public CommunityCommentResponse toCommentResponse(CommunityComment comment, boolean adminView) {
        return CommunityCommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .authorId(comment.getAuthorId())
                .authorName(comment.getAuthorName())
                .authorRole(comment.getAuthorRole())
                .content(comment.getContent())
                .parentId(comment.getParentId())
                .replyToUserId(comment.getReplyToUserId())
                .replyToUserName(comment.getReplyToUserName())
                .status(comment.getStatus())
                .shadowBanned(adminView ? Boolean.TRUE.equals(comment.getShadowBanned()) : null)
                .createdAt(DateTimeUtil.toIsoUtc(comment.getCreatedAt()))
                .build();
    }

    /** C 端视图（不回显影子封禁标记） */
    public CommunityCommentResponse toCommentResponse(CommunityComment comment) {
        return toCommentResponse(comment, false);
    }

    public ContentReportResponse toReportResponse(ContentReport report, String targetContent, String targetAuthorName) {
        return ContentReportResponse.builder()
                .id(report.getId())
                .reporterId(report.getReporterId())
                .reporterName(report.getReporterName())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reasonType(report.getReasonType())
                .reasonDetail(report.getReasonDetail())
                .status(report.getStatus())
                .communityId(report.getCommunityId())
                .propertyCompanyId(report.getPropertyCompanyId())
                .handlerId(report.getHandlerId())
                .handlerName(report.getHandlerName())
                .handleRemark(report.getHandleRemark())
                .handledAt(DateTimeUtil.toIsoUtc(report.getHandledAt()))
                .createdAt(DateTimeUtil.toIsoUtc(report.getCreatedAt()))
                .targetContent(targetContent)
                .targetAuthorName(targetAuthorName)
                .build();
    }

    public List<String> parseList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public String toJson(List<String> list) {
        if (list == null) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }
}
