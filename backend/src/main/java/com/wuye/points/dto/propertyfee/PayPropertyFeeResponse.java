package com.wuye.points.dto.propertyfee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "物业费支付响应")
public class PayPropertyFeeResponse {

    @Schema(description = "物业费ID", example = "pf_r8k2m4")
    private String propertyFeeId;

    @Schema(description = "本次支付金额", example = "1500.00")
    private BigDecimal paidAmount;

    @Schema(description = "累计已付金额", example = "3000.00")
    private BigDecimal totalPaidAmount;

    @Schema(description = "剩余未付金额", example = "0.00")
    private BigDecimal remainingAmount;

    @Schema(description = "支付状态", example = "已支付")
    private String paymentStatus;

    @Schema(description = "支付方式", example = "微信支付")
    private String paymentMethod;

    @Schema(description = "使用积分", example = "100")
    private Integer pointUsed;

    @Schema(description = "使用物业币", example = "50.00")
    private BigDecimal coinUsed;

    @Schema(description = "现金支付金额", example = "1450.00")
    private BigDecimal cashAmount;

    @Schema(description = "支付时间", example = "2026-01-01T00:00:00.000Z")
    private String paidAt;
}
