package com.wuye.points.dto.coin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "解冻物业币响应")
public class UnfreezeCoinResponse {

    @Schema(description = "用户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "解冻金额", example = "50.00")
    private BigDecimal unfrozenAmount;

    @Schema(description = "新余额", example = "170.50")
    private BigDecimal newBalance;

    @Schema(description = "解冻时间", example = "2026-01-01T00:00:00.000Z")
    private String unfrozenAt;

    @Schema(description = "操作人ID", example = "admin_001")
    private String operatorId;
}
