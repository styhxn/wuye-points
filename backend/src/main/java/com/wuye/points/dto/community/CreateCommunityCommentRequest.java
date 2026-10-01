package com.wuye.points.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "评论请求")
public class CreateCommunityCommentRequest {

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容不能超过1000字")
    private String content;

    @Schema(description = "被回复评论ID（二级回复时填写）")
    private String parentId;

    @Schema(description = "被回复用户ID")
    private String replyToUserId;

    @Schema(description = "被回复用户名称")
    private String replyToUserName;
}
