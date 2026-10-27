package com.wuye.points.dto.distributor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "一级代理商用户购买记录项")
public class DistributorOrderItemResponse {

    @Schema(description = "订单ID", example = "ord_xxx")
    private String orderId;

    @Schema(description = "订单号", example = "202610250001")
    private String orderNo;

    @Schema(description = "订单状态", example = "completed")
    private String orderStatus;

    @Schema(description = "成交金额（零售价×件数，不含运费）", example = "39.90")
    private BigDecimal totalAmount;

    @Schema(description = "商品件数", example = "2")
    private Integer quantity;

    @Schema(description = "商品名称", example = "五常大米 5kg")
    private String productName;

    @Schema(description = "商品封面")
    private String productCover;

    @Schema(description = "代销的业主小店名称")
    private String shopName;

    @Schema(description = "购买人姓名（脱敏）", example = "张*")
    private String buyerName;

    @Schema(description = "购买人电话（脱敏）", example = "138****8000")
    private String buyerPhone;

    @Schema(description = "收货地址/地理位置", example = "幸福小区3栋2单元501")
    private String address;

    @Schema(description = "下单时间", example = "2026-10-25T10:00:00.000Z")
    private String createdAt;

    @Schema(description = "结算归属方（固定 distributor）", example = "distributor")
    private String settlementOwner;
}
