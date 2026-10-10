package com.wuye.points.dto.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "完成配送响应")
public class CompleteDeliveryResponse {

    @Schema(description = "配送ID", example = "del_001")
    private String id;

    @Schema(description = "订单ID", example = "order_001")
    private String orderId;

    @Schema(description = "配送状态", example = "completed", allowableValues = {"pending", "accepted", "delivering", "completed", "failed", "property_fallback"})
    private String status;

    @Schema(description = "配送员收入", example = "5.00")
    private BigDecimal courierEarning;

    @Schema(description = "配送费用", example = "5.00")
    private BigDecimal fee;

    @Schema(description = "满额减免前基础配送费", example = "5.00")
    private BigDecimal originalDeliveryFee;

    @Schema(description = "减免承担方 none/merchant/property/platform", example = "merchant")
    private String deliverySubsidySponsor;

    @Schema(description = "配送补贴金额", example = "0.00")
    private BigDecimal deliverySubsidyAmount;

    @Schema(description = "配送结算基数 = 实付配送费 + 配送补贴", example = "5.00")
    private BigDecimal deliverySettlementBase;

    @Schema(description = "公司配送收入 = 基数 × platformDeliveryShareRate", example = "0.50")
    private BigDecimal platformDeliveryShare;

    @Schema(description = "兼容旧字段，等价于 courierEarning", example = "4.00")
    private BigDecimal courierShare;

    @Schema(description = "结算状态 pending/settled/reversed", example = "pending")
    private String settlementStatus;

    @Schema(description = "配送完成凭证图片URL列表")
    private List<String> proofImageUrls;

    @Schema(description = "配送完成备注", example = "已送达")
    private String remark;

    @Schema(description = "商家自配配送费分成（仅 carrierType=merchant 的配送单有值）", example = "4.50")
    private BigDecimal merchantDeliveryFeeShare;

    @Schema(description = "承运类型 courier/merchant", example = "courier")
    private String carrierType;

    @Schema(description = "送达时间", example = "2024-01-01T11:00:00")
    private String deliveredAt;
}
