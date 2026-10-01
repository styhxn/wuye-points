package com.wuye.points.dto.distributor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "一级经销商-下游拿货/上架记录条目")
public class DistributorPickupItemResponse {

    @Schema(description = "拿货上架记录ID")
    private String listingId;

    @Schema(description = "下游业主商户申请ID")
    private String residentMerchantId;

    @Schema(description = "下游住户ID")
    private String residentId;

    @Schema(description = "拿货人姓名（脱敏：中间字以*隐藏）")
    private String residentName;

    @Schema(description = "拿货人联系电话（脱敏）", example = "138****1234")
    private String residentPhone;

    @Schema(description = "商品ID")
    private String distributorProductId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "下游零售价")
    private BigDecimal retailPrice;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "分享次数")
    private Integer shareCount;

    @Schema(description = "拿货时间")
    private LocalDateTime createdAt;
}
