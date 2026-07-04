package com.wuye.points.dto.admin.coinfreeze;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "管理员冻结积分请求")
public class AdminFreezeCoinRequest {

    @NotNull(message = "冻结金额不能为空")
    @DecimalMin(value = "0", message = "冻结金额不能为负")
    @Schema(description = "冻结金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal amount;

    @NotBlank(message = "冻结原因不能为空")
    @Size(min = 1, max = 200, message = "冻结原因长度需在1-200之间")
    @Schema(description = "冻结原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "违规操作冻结")
    private String reason;
}
