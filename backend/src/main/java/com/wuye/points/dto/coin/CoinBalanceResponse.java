package com.wuye.points.dto.coin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "物业币余额响应")
public class CoinBalanceResponse {

    @Schema(description = "个人物业币余额", example = "120.50")
    private BigDecimal coinBalance;

    @Schema(description = "家庭物业币余额", example = "500.00")
    private BigDecimal familyCoinBalance;

    @Schema(description = "家庭ID", example = "fam_abc123")
    private String familyId;

    @Schema(description = "物业币是否冻结", example = "false")
    private Boolean coinFrozen;

    @Schema(description = "物业币是否隐藏", example = "false")
    private Boolean coinHidden;

    @Schema(description = "物业币显示是否启用", example = "true")
    private Boolean coinDisplayEnabled;

    @Schema(description = "今日获得物业币", example = "10.00")
    private BigDecimal todayEarned;

    @Schema(description = "今日消费物业币", example = "5.00")
    private BigDecimal todaySpent;

    @Schema(description = "即将过期物业币", example = "20.00")
    private BigDecimal expiringSoon;

    @Schema(description = "过期日期", example = "2026-09-01")
    private String expiryDate;
}
