package com.wuye.points.dto.profilereward;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
@Schema(description = "创建信息采集奖励配置请求")
public class CreateRewardConfigRequest {

    @NotBlank(message = "scope不能为空")
    @Schema(description = "作用域", example = "property_company",
            allowableValues = {"property_company", "community", "platform", "coordinator"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String scope;

    @NotBlank(message = "scopeId不能为空")
    @Schema(description = "作用域ID", example = "pc_abc12345", requiredMode = Schema.RequiredMode.REQUIRED)
    private String scopeId;

    @NotBlank(message = "字段名不能为空")
    @Schema(description = "字段名", example = "birthday", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fieldName;

    @NotNull(message = "奖励积分不能为空")
    @PositiveOrZero(message = "奖励积分不能为负数")
    @Schema(description = "奖励积分数", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer rewardPoints;

    @Schema(description = "是否每用户仅奖励一次", example = "true")
    private Boolean oncePerUser;
}
