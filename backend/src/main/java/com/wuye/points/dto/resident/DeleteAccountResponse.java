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

    @Schema(description = "注销状态", example = "pending")
    private String status;

    @Schema(description = "提示信息", example = "账号注销申请已提交，请在15天冷静期内确认")
    private String message;

    @Schema(description = "注销请求ID", example = "del_001")
    private String requestId;

    @Schema(description = "冷静期过期时间", example = "2026-07-19T15:00:00")
    private String expiresAt;
}
