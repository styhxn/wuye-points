package com.wuye.points.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "积分兑换响应")
public class RedeemPointResponse {

    @Schema(description = "使用积分数", example = "100")
    private Integer pointsUsed;

    @Schema(description = "兑换金额", example = "10.00")
    private BigDecimal redeemedAmount;

    @Schema(description = "操作后余额", example = "400")
    private Integer balanceAfter;

    @Schema(description = "操作后家庭余额", example = "1100")
    private Integer familyBalanceAfter;

    @Schema(description = "兑换时间", example = "2026-01-01T00:00:00.000Z")
    private String redeemedAt;
}
