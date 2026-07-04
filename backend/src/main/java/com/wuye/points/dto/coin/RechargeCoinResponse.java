package com.wuye.points.dto.coin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "充值物业币响应")
public class RechargeCoinResponse {

    @Schema(description = "记录ID", example = "cr_abc123")
    private String recordId;

    @Schema(description = "用户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "充值金额", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "操作后余额", example = "220.50")
    private BigDecimal balanceAfter;

    @Schema(description = "操作后家庭余额", example = "600.00")
    private BigDecimal familyBalanceAfter;

    @Schema(description = "充值时间", example = "2026-01-01T00:00:00.000Z")
    private String rechargedAt;
}
