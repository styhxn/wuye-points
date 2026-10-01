package com.wuye.points.dto.profilereward;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "触发信息采集奖励请求")
public class TriggerRewardRequest {

    @NotBlank(message = "字段名不能为空")
    @Schema(description = "字段名", example = "birthday", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fieldName;
}
