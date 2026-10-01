package com.wuye.points.dto.admin.rolewithdrawal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "审核配送员/多角色提现请求")
@Data
public class AuditRoleWithdrawalRequest {

    @Schema(description = "审核结果", example = "approved", allowableValues = {"approved", "rejected", "completed"})
    @NotBlank(message = "审核结果不能为空")
    private String auditResult;

    @Schema(description = "转账凭证", example = "https://proof.example.com/img.jpg")
    private String transferProof;

    @Schema(description = "拒绝原因（拒绝时必填）", example = "金额超限")
    private String rejectReason;

    @Schema(description = "备注", example = "已转账")
    private String remark;
}
