package com.wuye.points.dto.admin.coinwithdrawal;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuditCoinWithdrawalRequest {

    @NotBlank(message = "审核结果不能为空")
    private String auditResult;

    private String transferProof;
    private String rejectReason;
    private String remark;
}
