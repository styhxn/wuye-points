package com.wuye.points.dto.admin.coinexpiry;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "执行积分过期响应")
public class RunCoinExpiryResponse {

    @Schema(description = "任务ID", example = "TASK001")
    private String taskId;

    @Schema(description = "过期日期", example = "2026-01-01")
    private String expiryDate;

    @Schema(description = "是否试运行", example = "false")
    private boolean dryRun;

    @Schema(description = "处理记录数", example = "50")
    private int processedCount;

    @Schema(description = "总过期金额", example = "5000.00")
    private BigDecimal totalExpiredAmount;

    @Schema(description = "转入公共池金额", example = "4000.00")
    private BigDecimal clearedToPool;

    @Schema(description = "任务状态", example = "completed")
    private String status;

    @Schema(description = "执行时间", example = "2026-01-01 12:00:00")
    private String executedAt;
}
