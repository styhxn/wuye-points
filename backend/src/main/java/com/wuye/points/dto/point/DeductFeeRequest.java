package com.wuye.points.dto.point;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeductFeeRequest {

    @NotBlank(message = "物业费账单ID不能为空")
    private String propertyFeeId;

    @NotNull(message = "抵扣积分数量不能为空")
    @Min(value = 1, message = "抵扣积分数量至少为1")
    private Integer points;

    private String familyId;
}
