package com.wuye.points.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "消息列表项响应")
public class MessageListItemResponse {

    @Schema(description = "消息ID", example = "msg_r7m9x1")
    private String id;

    @Schema(description = "发送者ID", example = "res_b7m9x1")
    private String fromId;

    @Schema(description = "发送者姓名", example = "张三")
    private String fromName;

    @Schema(description = "发送者头像URL", example = "https://example.com/avatar.jpg")
    private String fromAvatarUrl;

    @Schema(description = "接收者ID", example = "res_k3p5n2")
    private String toId;

    @Schema(description = "接收者角色", example = "resident", allowableValues = {"resident", "property_admin", "platform_admin", "merchant", "courier", "coordinator", "sector_leader", "individual_leader", "activity_leader", "technician"})
    private String toRole;

    @Schema(description = "消息类型", example = "property_message", allowableValues = {"property_message", "platform_message", "system_message", "community_message", "resident_message", "technician_message"})
    private String messageType;

    @Schema(description = "消息内容", example = "你好，请问有空吗？")
    private String content;

    @Schema(description = "聊天类型", example = "SINGLE", allowableValues = {"SINGLE", "GROUP", "BROADCAST"})
    private String chatType;

    @Schema(description = "群组ID", example = "grp_xyz789")
    private String groupId;

    @Schema(description = "已读状态", example = "READ", allowableValues = {"READ", "UNREAD"})
    private String readStatus;

    @Schema(description = "物业公司ID", example = "prop_a1c3e5")
    private String propertyCompanyId;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;
}
