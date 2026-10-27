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

    @Schema(description = "店主佣金（与 resellerProfit 同口径，便于前端统一展示）", example = "12.00")
    private BigDecimal commissionAmount;

    @Schema(description = "一级代理商商家ID（结算主体）", example = "mer_xxx")
    private String distributorMerchantId;

    @Schema(description = "订单来源：distributor=代理货源代销", example = "distributor")
    private String sourceType;

    @Schema(description = "结算归属方：distributor=一级代理商", example = "distributor")
    private String settlementOwner;

    private String status;
    private String settleAt;
    private String createdAt;
}
