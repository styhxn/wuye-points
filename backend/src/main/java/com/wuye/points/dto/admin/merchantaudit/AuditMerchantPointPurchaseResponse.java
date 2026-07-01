package com.wuye.points.dto.admin.merchantaudit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditMerchantPointPurchaseResponse {

    private String id;
    private String auditStatus;
    private Integer pointAmount;
    private Integer actualPointAmount;
    private Integer merchantPointBalance;
    private String auditedAt;
    private String auditorId;
}
