package com.wuye.points.dto.admin.rolewithdrawal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "配送员/多角色提现审批汇总响应（看板用）")
public class AdminRoleWithdrawalSummaryResponse {

    @Schema(description = "待审笔数", example = "5")
    private Long pendingCount;

    @Schema(description = "待审金额合计（申请金额）", example = "500.00")
    private BigDecimal pendingAmount;

    @Schema(description = "今日已审笔数（按 auditedAt）", example = "3")
    private Long todayProcessedCount;

    @Schema(description = "今日已审金额合计（实到金额）", example = "200.00")
    private BigDecimal todayProcessedAmount;

    @Schema(description = "已通过笔数（approved+completed）", example = "12")
    private Long approvedCount;

    @Schema(description = "已通过金额合计（实到金额）", example = "1200.00")
    private BigDecimal approvedAmount;
}
