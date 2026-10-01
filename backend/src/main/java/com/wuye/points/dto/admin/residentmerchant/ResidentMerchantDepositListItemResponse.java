package com.wuye.points.dto.admin.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "业主商户保证金列表项")
public class ResidentMerchantDepositListItemResponse {

    private String id;
    private String applicationId;
    private String residentId;
    private String residentName;
    private BigDecimal amount;
    private String status;
    private String paymentMethod;
    private String paidAt;
    private BigDecimal deductedAmount;
}
