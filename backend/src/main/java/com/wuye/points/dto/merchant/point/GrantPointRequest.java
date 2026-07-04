package com.wuye.points.dto.merchant.point;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "商家录入积分请求")
@Data
public class GrantPointRequest {

    @Schema(description = "购买记录ID", example = "pur_xxx", required = true)
    @NotBlank(message = "购买记录ID不能为空")
    private String purchaseId;

    @Schema(description = "住户ID", example = "res_xxx", required = true)
    @NotBlank(message = "住户ID不能为空")
    private String residentId;

    @Schema(description = "录入积分数量", example = "100", required = true)
    @NotNull(message = "录入积分数量不能为空")
    @Min(value = 1, message = "录入积分数量至少为1")
    private Integer pointAmount;

    @Schema(description = "描述")
    private String description;
}
