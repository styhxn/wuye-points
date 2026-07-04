package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "我的金币响应")
public class MyCoinsResponse {

    @Schema(description = "住户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "个人金币余额", example = "500.00")
    private BigDecimal coinBalance;

    @Schema(description = "金币是否冻结", example = "false")
    private Boolean coinFrozen;

    @Schema(description = "金币是否隐藏", example = "false")
    private Boolean coinHidden;

    @Schema(description = "金币展示是否开启", example = "true")
    private Boolean coinDisplayEnabled;

    @Schema(description = "家庭ID", example = "fam_ghi789")
    private String familyId;

    @Schema(description = "家庭金币余额", example = "2000.00")
    private BigDecimal familyCoinBalance;

    @Schema(description = "本月获得金币", example = "100.00")
    private BigDecimal earnedThisMonth;

    @Schema(description = "本月消耗金币", example = "50.00")
    private BigDecimal spentThisMonth;

    @Schema(description = "即将过期金币", example = "30.00")
    private BigDecimal expiringSoon;

    @Schema(description = "金币过期日期", example = "2026-01-01T00:00:00.000Z")
    private String expiryDate;
}
