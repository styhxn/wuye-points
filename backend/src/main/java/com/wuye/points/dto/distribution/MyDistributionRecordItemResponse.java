package com.wuye.points.dto.distribution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "我的分成记录（仅本人份额，角色自动收敛）")
public class MyDistributionRecordItemResponse {

    @Schema(description = "分成记录ID")
    private String id;

    @Schema(description = "订单ID")
    private String orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "商家名称")
    private String merchantName;

    @Schema(description = "订单总额")
    private BigDecimal totalAmount;

    @Schema(description = "抽佣比例")
    private BigDecimal commissionRate;

    @Schema(description = "我的分成类型：merchant/property/coordinator/sector_leader/individual_leader/building_leader")
    private String shareType;

    @Schema(description = "我的分成金额")
    private BigDecimal myShareAmount;

    @Schema(description = "分成生成时间")
    private String createdAt;
}
