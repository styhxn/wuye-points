package com.wuye.points.dto.point;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RedeemPointRequest {

    @NotNull(message = "兑换积分数量不能为空")
    @Min(value = 100, message = "兑换积分数量至少为100")
    private Integer points;
}
