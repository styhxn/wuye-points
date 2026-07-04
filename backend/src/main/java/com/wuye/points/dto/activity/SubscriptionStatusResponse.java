package com.wuye.points.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "订阅状态响应")
@Data
@Builder
public class SubscriptionStatusResponse {

    @Schema(description = "是否已订阅", example = "true")
    private boolean subscribed;

    @Schema(description = "订阅类型", example = "MONTHLY")
    private String subscriptionType;

    @Schema(description = "开始日期", example = "2026-01-01T00:00:00.000Z")
    private String startDate;

    @Schema(description = "结束日期", example = "2026-02-01T00:00:00.000Z")
    private String endDate;

    @Schema(description = "状态", allowableValues = {"ACTIVE", "EXPIRED", "CANCELLED"})
    private String status;
}
