package com.wuye.points.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建消息请求")
public class CreateMessageRequest {

    @Schema(description = "接收者ID", example = "res_b7m9x1")
    private String toId;

    @NotBlank(message = "消息内容不能为空")
    @Size(min = 1, max = 2000, message = "消息内容长度须在1-2000字符之间")
    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "你好，请问有空吗？")
    private String content;

    @NotBlank(message = "messageType不能为空")
    @Schema(description = "消息类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "property_message", allowableValues = {"property_message", "platform_message", "system_message", "community_message", "resident_message", "technician_message"})
    private String messageType;

    @NotBlank(message = "chatType不能为空")
    @Schema(description = "聊天类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "SINGLE", allowableValues = {"SINGLE", "GROUP", "BROADCAST"})
    private String chatType;

    @Schema(description = "目标角色", example = "RESIDENT")
    private String toRole;

    @Schema(description = "群组ID", example = "grp_xyz789")
    private String groupId;
}
