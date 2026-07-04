package com.wuye.points.dto.admin.merchantaudit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "审核商家提现请求")
@Data
public class AuditMerchantWithdrawalRequest {

    @Schema(description = "审核结果", example = "approved", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "审核结果不能为空")
    private String auditResult;

    @Schema(description = "转账凭证", example = "https://proof.example.com/img.jpg")
    private String transferProof;

    @Schema(description = "拒绝原因", example = "金额超限")
    private String rejectReason;

    @Schema(description = "备注", example = "已转账")
    private String remark;
}
