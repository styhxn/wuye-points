package com.wuye.points.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "获取积分响应")
public class EarnPointResponse {

    @Schema(description = "记录ID", example = "pr_abc123")
    private String recordId;

    @Schema(description = "用户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "获得积分数量", example = "50")
    private Integer pointAmount;

    @Schema(description = "操作后余额", example = "550")
    private Integer balanceAfter;

    @Schema(description = "操作后家庭余额", example = "1250")
    private Integer familyBalanceAfter;

    @Schema(description = "来源", example = "签到")
    private String source;

    @Schema(description = "操作人ID", example = "sys_admin")
    private String operatorId;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;
}
