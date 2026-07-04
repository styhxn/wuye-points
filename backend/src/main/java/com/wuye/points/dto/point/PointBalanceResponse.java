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
}
