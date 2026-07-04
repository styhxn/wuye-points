package com.wuye.points.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "积分抵扣物业费响应")
public class DeductFeeResponse {

    @Schema(description = "物业费账单ID", example = "pf_abc123")
    private String propertyFeeId;

    @Schema(description = "使用积分数", example = "100")
    private Integer pointsUsed;

    @Schema(description = "抵扣金额", example = "10.00")
    private BigDecimal deductedAmount;

    @Schema(description = "剩余应付金额", example = "90.00")
    private BigDecimal remainingAmount;

    @Schema(description = "支付状态", example = "partial")
    private String paymentStatus;

    @Schema(description = "用户积分余额", example = "400")
    private Integer residentBalance;

    @Schema(description = "家庭积分余额", example = "1100")
    private Integer familyBalance;

    @Schema(description = "抵扣时间", example = "2026-01-01T00:00:00.000Z")
    private String deductedAt;
}
