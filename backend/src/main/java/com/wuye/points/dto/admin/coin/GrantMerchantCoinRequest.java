package com.wuye.points.dto.admin.coin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "给商家发放物业币请求")
public class GrantMerchantCoinRequest {

    @NotNull(message = "物业币数量不能为空")
    @DecimalMin(value = "0.01", message = "物业币数量至少为0.01")
    @Schema(description = "发放物业币数量", required = true, example = "10.00")
    private BigDecimal coinAmount;

    @Schema(description = "来源（可选，默认 manual）", example = "manual")
    private String source;

    @Size(max = 200, message = "描述长度不能超过200")
    @Schema(description = "描述", example = "管理端给商家发放物业币")
    private String description;

    @Schema(description = "来源ID", example = "admin_grant_abc")
    private String sourceId;
}
