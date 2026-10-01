package com.wuye.points.dto.profilereward;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "触发信息采集奖励响应")
public class TriggerRewardResponse {

    @Schema(description = "字段名", example = "birthday")
    private String fieldName;

    @Schema(description = "奖励积分数，0表示未奖励", example = "10")
    private Integer rewardedPoints;

    @Schema(description = "消息", example = "奖励已发放")
    private String message;
}
