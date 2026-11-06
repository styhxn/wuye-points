package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "撤回账号注销申请响应")
@Data
@Builder
public class CancelDeletionResponse {

    @Schema(description = "住户ID", example = "res_xxx")
    private String residentId;

    @Schema(description = "撤回后的账号状态", example = "active")
    private String status;

    @Schema(description = "提示信息", example = "已撤回注销申请，账号恢复正常使用")
    private String message;
}
