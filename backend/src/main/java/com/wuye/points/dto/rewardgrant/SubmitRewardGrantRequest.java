package com.wuye.points.dto.rewardgrant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "提交奖励金/积分发放申请")
public class SubmitRewardGrantRequest {

    @Schema(description = "目标住户ID（积分发放对象）", example = "res_xxx", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "目标住户ID不能为空")
    private String targetResidentId;

    @Schema(description = "发放类型 points(积分)/reward(奖励金)", example = "points", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "发放类型不能为空")
    private String grantType;

    @Schema(description = "积分数量", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "积分数量不能为空")
    @Min(value = 1, message = "积分数量至少为1")
    private Integer pointAmount;

    @Schema(description = "申请理由", example = "社区活动优秀住户奖励")
    private String reason;
}
