package com.wuye.points.dto.admin.merchantaudit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditMerchantWithdrawalResponse {

    private String id;
    private String auditStatus;
    private String transferProof;
    private String auditedAt;
    private String auditorId;
    private String completedAt;
}
