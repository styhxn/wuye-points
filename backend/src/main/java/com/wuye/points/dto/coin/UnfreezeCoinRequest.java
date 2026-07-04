package com.wuye.points.dto.coin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "解冻物业币请求")
public class UnfreezeCoinRequest {

    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID", required = true, example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "冻结记录ID", example = "fc_abc123")
    private String frozenRecordId;

    @NotBlank(message = "解冻原因不能为空")
    @Size(min = 1, max = 200, message = "解冻原因长度需在1-200之间")
    @Schema(description = "解冻原因", required = true, example = "误操作解冻")
    private String reason;
}
