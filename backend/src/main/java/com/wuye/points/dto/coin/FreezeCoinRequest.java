package com.wuye.points.dto.coin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "冻结物业币请求")
public class FreezeCoinRequest {

    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID", required = true, example = "res_b7m9x1")
    private String residentId;

    @DecimalMin(value = "0", message = "冻结金额不能为负")
    @Schema(description = "冻结金额", example = "50.00")
    private BigDecimal amount;

    @NotBlank(message = "冻结原因不能为空")
    @Size(min = 1, max = 200, message = "冻结原因长度需在1-200之间")
    @Schema(description = "冻结原因", required = true, example = "违规操作冻结")
    private String reason;
}
