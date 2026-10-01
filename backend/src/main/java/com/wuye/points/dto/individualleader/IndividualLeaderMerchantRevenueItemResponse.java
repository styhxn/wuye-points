package com.wuye.points.dto.individualleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "个体负责人-所负责商家营收条目")
public class IndividualLeaderMerchantRevenueItemResponse {

    @Schema(description = "商家ID")
    private String merchantId;

    @Schema(description = "商家名称")
    private String merchantName;

    @Schema(description = "商家审核状态")
    private String auditStatus;

    @Schema(description = "营收总额")
    private BigDecimal revenue;

    @Schema(description = "订单笔数")
    private long orderCount;

    @Schema(description = "我的分成金额")
    private BigDecimal myShare;
}
