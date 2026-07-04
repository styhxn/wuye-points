package com.wuye.points.dto.websocket;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "WebSocket消息")
public class WebSocketMessage {

    @Schema(description = "消息类型", example = "CHAT", allowableValues = {"CHAT", "NOTIFICATION", "HEARTBEAT", "SYSTEM"})
    private String type;

    @Schema(description = "消息负载")
    private Object payload;

    @Schema(description = "时间戳", example = "2026-01-01T00:00:00.000Z")
    private String timestamp;

    @Schema(description = "消息ID", example = "ws_msg_r7m9x1")
    private String messageId;
}
