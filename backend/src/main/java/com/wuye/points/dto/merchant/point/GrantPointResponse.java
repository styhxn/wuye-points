package com.wuye.points.dto.merchant.point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "商家录入积分响应")
@Data
@Builder
public class GrantPointResponse {

    @Schema(description = "ID", example = "gp_xxx")
    private String id;

    @Schema(description = "商家ID", example = "mer_xxx")
    private String merchantId;

    @Schema(description = "购买记录ID", example = "pur_xxx")
    private String purchaseId;

    @Schema(description = "住户ID", example = "res_xxx")
    private String residentId;

    @Schema(description = "录入积分数量", example = "100")
    private Integer pointAmount;

    @Schema(description = "兑换比率", example = "1.00")
    private BigDecimal exchangeRate;

    @Schema(description = "购买剩余积分", example = "500")
    private Integer purchaseRemainingPoints;

    @Schema(description = "住户积分余额(操作后)", example = "600")
    private Integer residentBalanceAfter;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;
}
