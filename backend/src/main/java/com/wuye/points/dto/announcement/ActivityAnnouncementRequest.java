package com.wuye.points.dto.announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "活动内容发布请求")
public class ActivityAnnouncementRequest {

    @NotBlank(message = "title不能为空")
    @Size(max = 200, message = "title长度不能超过200")
    @Schema(description = "公告标题", example = "周末社区跑活动", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "content不能为空")
    @Schema(description = "公告内容", example = "本周六早上8点社区跑活动", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "小区ID", example = "com_001")
    private String communityId;

    @Schema(description = "活动组ID", example = "act_001")
    private String activityGroupId;

    @Schema(description = "发布时间（ISO8601），不传则立即发布", example = "2024-01-01T10:00:00Z")
    private String publishedAt;
}
