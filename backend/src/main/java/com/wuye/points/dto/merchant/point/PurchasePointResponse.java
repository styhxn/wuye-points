package com.wuye.points.dto.merchant.point;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PurchasePointResponse {

    private String id;
    private String merchantId;
    private Integer pointAmount;
    private BigDecimal payAmount;
    private String auditStatus;
    private String createdAt;
}
