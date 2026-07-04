package com.wuye.points.dto.admin.coinwithdrawal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "审核积分提现请求")
public class AuditCoinWithdrawalRequest {

    @NotBlank(message = "审核结果不能为空")
    @Schema(description = "审核结果", requiredMode = Schema.RequiredMode.REQUIRED, example = "approved")
    private String auditResult;

    @Schema(description = "转账凭证", example = "https://example.com/proof.jpg")
    private String transferProof;

    @Schema(description = "拒绝原因", example = "信息不完整")
    private String rejectReason;

    @Schema(description = "备注", example = "正常审核通过")
    private String remark;
}
