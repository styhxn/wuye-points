package com.wuye.points.dto.coinwithdrawal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "阻止/解阻止提现请求")
public class BlockWithdrawalRequest {

    @NotNull(message = "blocked不能为空")
    @Schema(description = "是否阻止提现", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean blocked;

    @Schema(description = "阻止/解阻止原因", example = "违规操作")
    private String reason;
}
