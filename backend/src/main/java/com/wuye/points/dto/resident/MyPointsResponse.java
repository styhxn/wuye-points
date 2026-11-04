package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "我的积分响应")
public class MyPointsResponse {

    @Schema(description = "住户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "本小区是否启用积分功能", example = "true")
    private Boolean pointEnabled;

    @Schema(description = "个人积分余额", example = "1000")
    private Integer pointBalance;

    @Schema(description = "家庭ID", example = "fam_ghi789")
    private String familyId;

    @Schema(description = "家庭积分余额", example = "3500")
    private Integer familyPointBalance;

    @Schema(description = "本月获得积分", example = "200")
    private Integer earnedThisMonth;

    @Schema(description = "本月消耗积分", example = "150")
    private Integer spentThisMonth;

    @Schema(description = "即将过期积分", example = "100")
    private Integer expiringSoon;

    @Schema(description = "积分过期日期", example = "2026-01-01T00:00:00.000Z")
    private String expiryDate;

    @Schema(description = "是否开放积分转物业（物业公司开关，默认 false）", example = "false")
    private Boolean pointTransferEnabled;

    @Schema(description = "积分转物业所需最低入住年限", example = "5")
    private Integer pointTransferMinYears;

    @Schema(description = "当前是否满足积分转物业条件（开关开启且入住年限达标）", example = "false")
    private Boolean pointTransferEligible;

    @Schema(description = "不可转物业的原因；可转时为 null", example = "入住未满5年，暂不可将积分转物业费")
    private String pointTransferBlockedReason;
}
