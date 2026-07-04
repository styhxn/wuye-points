package com.wuye.points.dto.admin.coinwithdrawal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "审核积分提现响应")
public class AuditCoinWithdrawalResponse {

    @Schema(description = "提现记录ID", example = "WD001")
    private String id;

    @Schema(description = "审核状态", example = "approved")
    private String auditStatus;

    @Schema(description = "积分金额", example = "1000.00")
    private BigDecimal coinAmount;

    @Schema(description = "实际到账金额", example = "950.00")
    private BigDecimal actualAmount;

    @Schema(description = "审核时间", example = "2026-01-01 12:00:00")
    private String auditedAt;

    @Schema(description = "审核人ID", example = "ADMIN001")
    private String auditorId;
}
