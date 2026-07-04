package com.wuye.points.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "邻居聊天限额响应")
public class NeighborLimitResponse {

    @Schema(description = "住户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "每日限额", example = "50")
    private Integer dailyLimit;

    @Schema(description = "今日已使用次数", example = "5")
    private Integer usedToday;

    @Schema(description = "剩余次数", example = "45")
    private Integer remaining;

    @Schema(description = "重置时间", example = "2026-01-02T00:00:00.000Z")
    private String resetAt;
}
