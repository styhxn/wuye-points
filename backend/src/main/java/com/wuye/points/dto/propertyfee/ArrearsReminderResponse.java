package com.wuye.points.dto.propertyfee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "催缴通知统一发送响应（按渠道拆分统计）")
public class ArrearsReminderResponse {

    @Schema(description = "批次ID", example = "batch_xxx")
    private String batchId;

    @Schema(description = "唯一住户数（按 residentId 去重）", example = "120")
    private int recipientCount;

    @Schema(description = "至少一个渠道发送成功的唯一住户数", example = "116")
    private int notifiedCount;

    @Schema(description = "两个渠道都不具备发送条件的住户数", example = "3")
    private int skippedCount;

    @Schema(description = "具备条件但全部发送失败的住户数", example = "1")
    private int failedCount;

    @Schema(description = "各渠道发送结果（允许重叠，总人数按住户去重）")
    private Channels channels;

    @Data
    @Builder
    @Schema(description = "各渠道统计")
    public static class Channels {
        @Schema(description = "应用内消息统计")
        private ChannelResult inApp;
        @Schema(description = "微信订阅消息统计")
        private ChannelResult wechat;
    }

    @Data
    @Builder
    @Schema(description = "单渠道统计")
    public static class ChannelResult {
        @Schema(description = "成功数", example = "120")
        private int successCount;
        @Schema(description = "跳过数（不具备发送条件）", example = "0")
        private Integer skippedCount;
        @Schema(description = "失败数（具备条件但发送异常）", example = "0")
        private int failedCount;
    }
}
