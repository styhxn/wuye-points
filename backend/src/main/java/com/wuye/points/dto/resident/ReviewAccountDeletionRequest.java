package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "平台审核注销账号请求")
public class ReviewAccountDeletionRequest {

    @NotBlank(message = "action不能为空")
    @Schema(description = "审核动作：approve=通过并注销；reject=驳回", example = "approve", requiredMode = Schema.RequiredMode.REQUIRED)
    private String action;

    @Schema(description = "驳回原因（action=reject 时填写）", example = "资料不完整")
    private String rejectReason;
}
