package com.wuye.points.dto.coinfund;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "物业币现金资金台账记录")
public class PropertyCoinFundLedgerItemResponse {

    @Schema(description = "台账ID")
    private String id;

    @Schema(description = "物业公司ID")
    private String propertyCompanyId;

    @Schema(description = "记账类型：recharge_in/merchant_settlement/property_transfer/adjustment")
    private String entryType;

    @Schema(description = "方向：credit/debit")
    private String direction;

    @Schema(description = "发生金额")
    private BigDecimal amount;

    @Schema(description = "记账后余额")
    private BigDecimal balanceAfter;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务单号")
    private String bizId;

    @Schema(description = "充值住户ID")
    private String residentId;

    @Schema(description = "结算商家ID")
    private String merchantId;

    @Schema(description = "说明")
    private String description;

    @Schema(description = "操作人ID")
    private String operatorId;

    @Schema(description = "平台转物业转账凭证")
    private String transferProof;

    @Schema(description = "创建时间")
    private String createdAt;
}
