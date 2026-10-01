package com.wuye.points.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "社区帖子")
public class CommunityPostResponse {

    @Schema(description = "帖子ID")
    private String id;

    @Schema(description = "作者ID")
    private String authorId;

    @Schema(description = "作者昵称")
    private String authorName;

    @Schema(description = "作者角色")
    private String authorRole;

    @Schema(description = "帖子内容")
    private String content;

    @Schema(description = "图片URL列表")
    private List<String> imageUrls;

    @Schema(description = "状态 published/hidden/deleted")
    private String status;

    @Schema(description = "点赞数")
    private int likeCount;

    @Schema(description = "评论数")
    private int commentCount;

    @Schema(description = "当前用户是否已点赞")
    private boolean likedByMe;

    @Schema(description = "创建时间(ISO8601)")
    private String createdAt;

    @Schema(description = "更新时间(ISO8601)")
    private String updatedAt;
}
