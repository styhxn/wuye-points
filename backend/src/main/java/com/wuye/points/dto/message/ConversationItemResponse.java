package com.wuye.points.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "会话列表项（邻居/官方/服务需求/商家广告）")
public class ConversationItemResponse {

    @Schema(description = "对方会话ID（住户 res_ 或官方 official_）", example = "res_k3p5n2")
    private String peerId;

    @Schema(description = "对方姓名/展示名", example = "李*华")
    private String peerName;

    @Schema(description = "对方头像URL", example = "https://cdn.example.com/avatar/res_k3p5n2.jpg")
    private String peerAvatarUrl;

    @Schema(description = "会话类型：neighbor/official/service_request/merchant_ad", example = "neighbor")
    private String conversationType;

    @Schema(description = "官方发送方类型（仅 official）", example = "property")
    private String officialSenderType;

    @Schema(description = "官方展示名（仅 official）", example = "幸福物业")
    private String officialDisplayName;

    @Schema(description = "关联任务/订单/广告ID", example = "dmt_demo001")
    private String relatedTaskId;

    @Schema(description = "最后一条消息内容", example = "好的，明天见")
    private String lastMessage;

    @Schema(description = "最后一条消息时间", example = "2026-01-01T00:00:00.000Z")
    private String lastMessageTime;

    @Schema(description = "最后一条消息是否为本人发送", example = "true")
    private Boolean lastMessageFromMe;

    @Schema(description = "该会话未读消息数", example = "3")
    private Integer unreadCount;
}
