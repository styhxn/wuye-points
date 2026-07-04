package com.wuye.points.dto.admin.pointclear;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "执行积分清零响应")
public class RunPointClearResponse {

    @Schema(description = "任务ID", example = "TASK20240101001")
    private String taskId;

    @Schema(description = "清零基准日期", example = "2024-01-01")
    private String clearDate;

    @Schema(description = "回溯截止日期", example = "2023-01-01")
    private String lookbackDate;

    @Schema(description = "是否仅预览", example = "false")
    private boolean dryRun;

    @Schema(description = "已处理住户数", example = "50")
    private int processedCount;

    @Schema(description = "清零积分总数", example = "10000")
    private int totalClearedPoints;

    @Schema(description = "转入资金池金额", example = "900.00")
    private BigDecimal clearedToPool;

    @Schema(description = "任务状态", example = "completed")
    private String status;

    @Schema(description = "执行时间", example = "2024-01-01 10:00:00")
    private String executedAt;
}
