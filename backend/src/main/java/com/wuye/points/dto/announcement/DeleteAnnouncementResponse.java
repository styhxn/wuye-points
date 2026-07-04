package com.wuye.points.dto.announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "删除公告响应")
public class DeleteAnnouncementResponse {

    @Schema(description = "公告ID", example = "ann_001")
    private String id;

    @Schema(description = "公告状态", example = "deleted")
    private String status;
}
