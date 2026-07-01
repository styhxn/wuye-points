package com.wuye.points.dto.admin.merchantaudit;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MerchantWithdrawalListItemResponse {

    private String id;
    private String merchantId;
    private String merchantName;
    private BigDecimal amount;
    private BigDecimal feeRate;
    private BigDecimal feeAmount;
    private BigDecimal actualAmount;
    private String auditStatus;
    private String auditedAt;
    private String completedAt;
    private String createdAt;
}
