package com.wuye.points.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "积分余额响应")
public class PointBalanceResponse {

    @Schema(description = "个人积分余额", example = "500")
    private Integer pointBalance;

    @Schema(description = "家庭积分余额", example = "1200")
    private Integer familyPointBalance;

    @Schema(description = "家庭ID")
    private String familyId;

    @Schema(description = "今日获得积分", example = "20")
    private Integer todayEarned;

    @Schema(description = "今日消费积分", example = "0")
    private Integer todaySpent;

    @Schema(description = "即将过期积分", example = "100")
    private Integer expiringPoints;

    @Schema(description = "过期日期", example = "2026-09-01")
    private String expiringDate;

    @Schema(description = "是否开放积分转物业（物业公司开关，默认 false）", example = "false")
    private Boolean pointTransferEnabled;

    @Schema(description = "积分转物业所需最低入住年限", example = "5")
    private Integer pointTransferMinYears;

    @Schema(description = "当前是否满足积分转物业条件（开关开启且入住年限达标）", example = "false")
    private Boolean pointTransferEligible;

    @Schema(description = "不可转物业的原因；可转时为 null", example = "积分转物业费暂未开放")
    private String pointTransferBlockedReason;
}
