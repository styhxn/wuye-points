package com.wuye.points.dto.admin.coinwithdrawal;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CoinWithdrawalListItemResponse {

    private String id;
    private String merchantId;
    private String merchantName;
    private BigDecimal coinAmount;
    private BigDecimal cashAmount;
    private BigDecimal feeRate;
    private BigDecimal feeAmount;
    private BigDecimal actualAmount;
    private String auditStatus;
    private String createdAt;
}
