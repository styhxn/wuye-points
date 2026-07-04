package com.wuye.points.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "积分兑换请求")
public class RedeemPointRequest {

    @NotNull(message = "兑换积分数量不能为空")
    @Min(value = 100, message = "兑换积分数量至少为100")
    @Schema(description = "兑换积分数量", required = true, example = "100")
    private Integer points;
}
