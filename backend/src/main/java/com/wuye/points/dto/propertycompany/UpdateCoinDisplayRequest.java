package com.wuye.points.dto.propertycompany;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "物业币显示开关请求")
public class UpdateCoinDisplayRequest {

    @NotNull(message = "enabled不能为空")
    @Schema(description = "是否对住户显示物业币", example = "true")
    private Boolean enabled;
}
