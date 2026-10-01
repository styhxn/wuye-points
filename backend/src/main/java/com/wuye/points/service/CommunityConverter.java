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

    public CommunityPostResponse toPostResponse(CommunityPost post, boolean likedByMe) {
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
                .createdAt(DateTimeUtil.toIsoUtc(post.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(post.getUpdatedAt()))
                .build();
    }

    public CommunityCommentResponse toCommentResponse(CommunityComment comment) {
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
                .createdAt(DateTimeUtil.toIsoUtc(comment.getCreatedAt()))
                .build();
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
