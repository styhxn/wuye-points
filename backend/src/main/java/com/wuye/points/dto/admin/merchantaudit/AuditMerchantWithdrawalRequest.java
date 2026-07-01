package com.wuye.points.dto.admin.merchantaudit;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuditMerchantWithdrawalRequest {

    @NotBlank(message = "审核结果不能为空")
    private String auditResult;

    private String transferProof;
    private String rejectReason;
    private String remark;
}
