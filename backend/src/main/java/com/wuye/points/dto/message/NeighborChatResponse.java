package com.wuye.points.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "邻居聊天响应")
public class NeighborChatResponse {

    @Schema(description = "消息ID", example = "msg_r7m9x1")
    private String id;

    @Schema(description = "发送者ID", example = "res_b7m9x1")
    private String fromId;

    @Schema(description = "接收者ID", example = "res_k3p5n2")
    private String toId;

    @Schema(description = "消息类型", example = "property_message", allowableValues = {"property_message", "platform_message", "system_message", "community_message", "resident_message", "technician_message"})
    private String messageType;

    @Schema(description = "消息内容", example = "你好，方便聊一下吗？")
    private String content;

    @Schema(description = "聊天类型", example = "SINGLE", allowableValues = {"SINGLE", "GROUP", "BROADCAST"})
    private String chatType;

    @Schema(description = "已读状态", example = "UNREAD", allowableValues = {"READ", "UNREAD"})
    private String readStatus;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;

    @Schema(description = "每日剩余次数", example = "45")
    private Integer dailyLimitRemaining;

    @Schema(description = "今日已使用次数", example = "5")
    private Integer usedToday;

    @Schema(description = "剩余次数", example = "45")
    private Integer remaining;
}
