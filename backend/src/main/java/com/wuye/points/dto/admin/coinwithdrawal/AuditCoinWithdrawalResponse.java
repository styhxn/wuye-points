package com.wuye.points.dto.admin.coinwithdrawal;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AuditCoinWithdrawalResponse {

    private String id;
    private String auditStatus;
    private BigDecimal coinAmount;
    private BigDecimal actualAmount;
    private String auditedAt;
    private String auditorId;
}
