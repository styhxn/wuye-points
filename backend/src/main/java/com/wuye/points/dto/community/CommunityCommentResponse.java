package com.wuye.points.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "社区评论")
public class CommunityCommentResponse {

    @Schema(description = "评论ID")
    private String id;

    @Schema(description = "所属帖子ID")
    private String postId;

    @Schema(description = "作者ID")
    private String authorId;

    @Schema(description = "作者昵称")
    private String authorName;

    @Schema(description = "作者角色")
    private String authorRole;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "被回复评论ID（顶级评论为空）")
    private String parentId;

    @Schema(description = "被回复用户ID")
    private String replyToUserId;

    @Schema(description = "被回复用户名称")
    private String replyToUserName;

    @Schema(description = "状态 published/deleted")
    private String status;

    @Schema(description = "创建时间(ISO8601)")
    private String createdAt;
}
