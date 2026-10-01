package com.wuye.points.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RefundAuditRequest {

    @NotBlank(message = "审核动作不能为空")
    @Schema(description = "审核动作：approve=通过(执行退款), reject=不通过(驳回)",
            example = "approve", requiredMode = Schema.RequiredMode.REQUIRED)
    private String action;

    @Size(max = 200, message = "驳回原因不超过200字")
    @Schema(description = "驳回原因（action=reject 时必填）", example = "商品无质量问题，驳回退货")
    private String rejectReason;
}
