package com.wuye.points.dto.sectorleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "板块负责人-分成明细条目")
public class SectorLeaderEarningItemResponse {

    @Schema(description = "分成记录ID")
    private String id;

    @Schema(description = "订单ID")
    private String orderId;

    @Schema(description = "商家ID")
    private String merchantId;

    @Schema(description = "商家名称")
    private String merchantName;

    @Schema(description = "分成金额")
    private BigDecimal amount;

    @Schema(description = "分成时间")
    private LocalDateTime createdAt;
}
