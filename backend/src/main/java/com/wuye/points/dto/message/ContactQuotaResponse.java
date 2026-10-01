package com.wuye.points.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "今日新聊配额响应")
public class ContactQuotaResponse {

    @Schema(description = "每日上限（不同人数）", example = "3")
    private Integer dailyLimit;

    @Schema(description = "今日已使用人数", example = "1")
    private Integer usedCount;

    @Schema(description = "剩余人数", example = "2")
    private Integer remainingCount;

    @Schema(description = "重置时间", example = "2026-07-14 00:00:00")
    private String resetAt;
}
