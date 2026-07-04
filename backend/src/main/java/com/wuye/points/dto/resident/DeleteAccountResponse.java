package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "注销账号响应")
public class DeleteAccountResponse {

    @Schema(description = "住户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "注销状态", example = "success")
    private String status;

    @Schema(description = "提示信息", example = "账号注销成功")
    private String message;
}
