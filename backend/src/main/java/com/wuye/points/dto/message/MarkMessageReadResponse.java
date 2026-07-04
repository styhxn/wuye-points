package com.wuye.points.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "标记消息已读响应")
public class MarkMessageReadResponse {

    @Schema(description = "消息ID", example = "msg_r7m9x1")
    private String id;

    @Schema(description = "已读状态", example = "READ", allowableValues = {"READ", "UNREAD"})
    private String readStatus;

    @Schema(description = "更新时间", example = "2026-01-01T00:00:00.000Z")
    private String updatedAt;
}
