package com.wuye.points.dto.sectorleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "商家配送费设置响应")
public class MerchantDeliveryFeeResponse {

    @Schema(description = "商家ID")
    private String id;

    @Schema(description = "商家名称")
    private String name;

    @Schema(description = "配送费")
    private BigDecimal deliveryFee;

    @Schema(description = "满额免配送费")
    private BigDecimal freeDeliveryThreshold;

    @Schema(description = "状态")
    private String status;
}
