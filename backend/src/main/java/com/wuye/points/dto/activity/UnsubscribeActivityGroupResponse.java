package com.wuye.points.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "取消订阅活动组响应")
@Data
@Builder
public class UnsubscribeActivityGroupResponse {

    @Schema(description = "小组ID", example = "ag_xxx")
    private String groupId;

    @Schema(description = "订阅记录ID", example = "ags_xxx")
    private String subscriptionId;

    @Schema(description = "是否仍订阅", example = "false")
    private Boolean subscribed;

    @Schema(description = "订阅状态", example = "cancelled")
    private String status;

    @Schema(description = "订阅到期日期", example = "2026-12-31")
    private String endDate;

    @Schema(description = "取消时间", example = "2026-01-01T00:00:00.000Z")
    private String cancelledAt;
}
