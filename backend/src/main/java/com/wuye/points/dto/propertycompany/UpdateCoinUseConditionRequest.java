package com.wuye.points.dto.propertycompany;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "更新物业币使用条件请求")
public class UpdateCoinUseConditionRequest {

    @NotBlank(message = "condition不能为空")
    @Schema(description = "使用条件：none/point_threshold/property_fee_paid/manual_issue",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "point_threshold")
    private String condition;

    @Min(value = 0, message = "积分门槛不能为负数")
    @Schema(description = "积分门槛（condition=point_threshold 时生效）", example = "100")
    private Integer pointThreshold;
}
