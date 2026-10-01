package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "住户转给物业 - 物业币请求")
@Data
public class TransferToPropertyCoinRequest {

    @Schema(description = "转出物业币数量（>0）", example = "50.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "coinAmount不能为空")
    @DecimalMin(value = "0.01", message = "coinAmount必须大于0")
    private BigDecimal coinAmount;

    @Schema(description = "备注（可选）", example = "主动捐给物业做活动经费")
    @Size(max = 200, message = "remark长度不能超过200")
    private String remark;
}
