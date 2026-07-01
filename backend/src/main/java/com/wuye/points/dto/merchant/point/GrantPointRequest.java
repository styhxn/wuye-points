package com.wuye.points.dto.merchant.point;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GrantPointRequest {

    @NotBlank(message = "购买记录ID不能为空")
    private String purchaseId;

    @NotBlank(message = "住户ID不能为空")
    private String residentId;

    @NotNull(message = "录入积分数量不能为空")
    @Min(value = 1, message = "录入积分数量至少为1")
    private Integer pointAmount;

    private String description;
}
