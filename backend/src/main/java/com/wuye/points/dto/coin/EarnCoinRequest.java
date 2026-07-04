package com.wuye.points.dto.coin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "获取物业币请求")
public class EarnCoinRequest {

    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID", required = true, example = "res_b7m9x1")
    private String residentId;

    @NotNull(message = "物业币数量不能为空")
    @DecimalMin(value = "0.01", message = "物业币数量至少为0.01")
    @Schema(description = "物业币数量", required = true, example = "10.00")
    private BigDecimal coinAmount;

    @NotBlank(message = "来源不能为空")
    @Schema(description = "来源", required = true, example = "充值")
    private String source;

    @Size(max = 200, message = "描述长度不能超过200")
    @Schema(description = "描述", example = "充值获得物业币")
    private String description;

    @Schema(description = "来源ID", example = "recharge_abc123")
    private String sourceId;
}
