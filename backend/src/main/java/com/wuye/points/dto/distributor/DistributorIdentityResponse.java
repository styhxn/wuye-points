package com.wuye.points.dto.distributor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 当前用户一级代理身份（C端 GET /distributors/my/identity）
 * 未开通时 HTTP 200 + code 0，仅 isDistributor=false（「没有」是正常结果，不是权限错误）。
 */
@Data
@Builder
@Schema(description = "当前用户一级代理身份")
public class DistributorIdentityResponse {

    @Schema(description = "是否已开通一级代理")
    private Boolean isDistributor;

    @Schema(description = "一级代理商家载体ID（已开通时非空）")
    private String distributorMerchantId;

    @Schema(description = "商家载体名称（已开通时非空）")
    private String merchantName;

    @Schema(description = "商家载体审核状态（已开通时非空）")
    private String auditStatus;

    @Schema(description = "可提现余额（已开通时非空）")
    private BigDecimal withdrawableAmount;

    @Schema(description = "是否被禁止提现（已开通时非空）")
    private Boolean withdrawalBlocked;

    @Schema(description = "累计营收（已开通时非空）")
    private BigDecimal totalRevenue;

    @Schema(description = "累计订单数（已开通时非空）")
    private Long totalOrders;
}
