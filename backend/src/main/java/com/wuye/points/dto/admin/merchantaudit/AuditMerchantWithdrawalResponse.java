package com.wuye.points.dto.admin.merchantaudit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "审核商家提现响应")
@Data
@Builder
public class AuditMerchantWithdrawalResponse {

    @Schema(description = "提现记录ID", example = "withdrawal_001")
    private String id;

    @Schema(description = "审核状态", example = "approved")
    private String auditStatus;

    @Schema(description = "转账凭证", example = "https://proof.example.com/img.jpg")
    private String transferProof;

    @Schema(description = "审核时间", example = "2026-07-01 14:30:00")
    private String auditedAt;

    @Schema(description = "审核人ID", example = "admin_001")
    private String auditorId;

    @Schema(description = "完成时间", example = "2026-07-01 15:00:00")
    private String completedAt;
}
