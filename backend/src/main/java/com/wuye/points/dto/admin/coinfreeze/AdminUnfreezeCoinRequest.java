package com.wuye.points.dto.admin.coinfreeze;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "管理员解冻积分请求")
public class AdminUnfreezeCoinRequest {

    @Schema(description = "冻结记录ID", example = "FREEZE001")
    private String frozenRecordId;

    @NotBlank(message = "解冻原因不能为空")
    @Size(min = 1, max = 200, message = "解冻原因长度需在1-200之间")
    @Schema(description = "解冻原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "误操作解冻")
    private String reason;
}
