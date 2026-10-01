package com.wuye.points.dto.admin.coinwithdrawal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "物业币兑换审批汇总响应（看板用）")
public class CoinWithdrawalSummaryResponse {

    @Schema(description = "待审笔数", example = "3")
    private Long pendingCount;

    @Schema(description = "待审金额合计（实到金额）", example = "900.00")
    private BigDecimal pendingAmount;

    @Schema(description = "今日完成笔数（按 completedAt）", example = "2")
    private Long todayCompletedCount;

    @Schema(description = "今日完成金额合计（实到金额）", example = "600.00")
    private BigDecimal todayCompletedAmount;

    @Schema(description = "已通过笔数（approved+completed）", example = "8")
    private Long approvedCount;

    @Schema(description = "已通过金额合计（实到金额）", example = "3600.00")
    private BigDecimal approvedAmount;
}
