package com.wuye.points.dto.sectorleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "所负责商家-营收情况条目")
public class SectorLeaderMerchantRevenueItemResponse {

    @Schema(description = "商家ID")
    private String merchantId;

    @Schema(description = "商家名称")
    private String merchantName;

    @Schema(description = "营收总额")
    private BigDecimal revenue;

    @Schema(description = "订单笔数")
    private long orderCount;

    @Schema(description = "我的分成金额")
    private BigDecimal myShare;
}
