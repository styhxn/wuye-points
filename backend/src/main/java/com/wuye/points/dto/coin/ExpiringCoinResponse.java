package com.wuye.points.dto.coin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "即将过期物业币响应")
public class ExpiringCoinResponse {

    @Schema(description = "即将过期物业币", example = "20.00")
    private BigDecimal expiringSoon;

    @Schema(description = "过期日期", example = "2026-09-01")
    private String expiryDate;
}
