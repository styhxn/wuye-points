package com.wuye.points.dto.canteen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 社区食堂-购买额度试算响应（手续费内扣口径）
 */
@Data
@Builder
@Schema(description = "购买额度试算响应")
public class QuotaQuoteResponse {

    @Schema(description = "申请购买额度金额（= 支付金额）", example = "100.00")
    private BigDecimal quotaAmount;

    @Schema(description = "手续费比例", example = "0.02")
    private BigDecimal feeRate;

    @Schema(description = "手续费金额", example = "2.00")
    private BigDecimal feeAmount;

    @Schema(description = "实际到账额度 = quotaAmount - feeAmount", example = "98.00")
    private BigDecimal actualQuota;
}
