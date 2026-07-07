package com.wuye.points.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "未读消息数响应")
public class UnreadCountResponse {

    @Schema(description = "总未读消息数", example = "5")
    private Integer totalUnread;

    @Schema(description = "系统通知未读数", example = "2")
    private Integer systemUnread;

    @Schema(description = "物业通知未读数", example = "1")
    private Integer propertyUnread;

    @Schema(description = "商家消息未读数", example = "1")
    private Integer merchantUnread;

    @Schema(description = "邻居私聊未读数", example = "1")
    private Integer neighborUnread;
}
