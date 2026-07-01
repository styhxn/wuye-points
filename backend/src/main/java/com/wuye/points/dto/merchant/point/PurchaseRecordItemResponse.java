package com.wuye.points.dto.merchant.point;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PurchaseRecordItemResponse {

    private String id;
    private Integer pointAmount;
    private BigDecimal payAmount;
    private String auditStatus;
    private Integer remainingPoints;
    private String auditedAt;
    private String auditorId;
    private String createdAt;
}
