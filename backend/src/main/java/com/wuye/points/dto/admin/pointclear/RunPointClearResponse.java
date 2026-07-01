package com.wuye.points.dto.admin.pointclear;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RunPointClearResponse {

    private String taskId;
    private String clearDate;
    private String lookbackDate;
    private boolean dryRun;
    private int processedCount;
    private int totalClearedPoints;
    private BigDecimal clearedToPool;
    private String status;
    private String executedAt;
}
