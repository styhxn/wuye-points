package com.wuye.points.dto.admin.merchantaudit;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MerchantPointPurchaseListItemResponse {

    private String id;
    private String merchantId;
    private String merchantName;
    private Integer pointAmount;
    private BigDecimal payAmount;
    private String auditStatus;
    private Integer remainingPoints;
    private String auditedAt;
    private String auditorId;
    private String createdAt;
}
