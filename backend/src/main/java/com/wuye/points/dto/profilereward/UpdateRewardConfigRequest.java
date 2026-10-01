package com.wuye.points.dto.profilereward;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
@Schema(description = "更新信息采集奖励配置请求")
public class UpdateRewardConfigRequest {

    @PositiveOrZero(message = "奖励积分不能为负数")
    @Schema(description = "奖励积分数", example = "20")
    private Integer rewardPoints;

    @Schema(description = "是否每用户仅奖励一次", example = "true")
    private Boolean oncePerUser;

    @Schema(description = "状态", example = "active", allowableValues = {"active", "inactive"})
    private String status;
}
