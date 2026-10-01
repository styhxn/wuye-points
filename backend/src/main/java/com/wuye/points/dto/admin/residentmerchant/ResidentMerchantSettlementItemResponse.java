package com.wuye.points.dto.admin.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "业主商户结算明细项")
public class ResidentMerchantSettlementItemResponse {

    private String id;
    private String orderId;
    private String listingId;
    private String residentId;
    private BigDecimal orderAmount;
    private BigDecimal resellerProfit;
    private String status;
    private String settleAt;
    private String createdAt;
}
