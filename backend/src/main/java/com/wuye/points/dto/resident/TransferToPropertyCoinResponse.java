package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "住户转给物业 - 物业币响应")
@Data
@Builder
public class TransferToPropertyCoinResponse {

    @Schema(description = "流水ID", example = "coin_xxx")
    private String recordId;

    @Schema(description = "住户ID", example = "res_xxx")
    private String residentId;

    @Schema(description = "转出物业币数量", example = "50.00")
    private BigDecimal coinAmount;

    @Schema(description = "扣减后个人物业币余额", example = "49.50")
    private BigDecimal balanceAfter;

    @Schema(description = "转出时间", example = "2026-08-17T10:00:00Z")
    private String transferredAt;
}
