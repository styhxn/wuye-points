package com.wuye.points.dto.order;

import com.wuye.points.dto.canteen.DirectedBalanceResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "订单支付试算响应")
public class PreviewOrderResponse {

    @Schema(description = "订单应付总额（= productAmount + deliveryFee）", example = "100.00")
    private BigDecimal totalAmount;

    @Schema(description = "商品小计（= productAmount）", example = "95.00")
    private BigDecimal subtotal;

    @Schema(description = "商品金额（不含配送费，满额判断口径）", example = "95.00")
    private BigDecimal productAmount;

    @Schema(description = "配送费（用户实际支付）", example = "5.00")
    private BigDecimal deliveryFee;

    @Schema(description = "满额减免前基础配送费", example = "5.00")
    private BigDecimal originalDeliveryFee;

    @Schema(description = "满额减免金额", example = "0.00")
    private BigDecimal deliveryWaiverAmount;

    @Schema(description = "减免承担方 none/merchant/property/platform", example = "merchant")
    private String deliverySubsidySponsor;

    @Schema(description = "配送补贴金额（= 减免额，注入配送链路）", example = "0.00")
    private BigDecimal deliverySubsidyAmount;

    @Schema(description = "配送结算基数 = 配送费 + 补贴", example = "5.00")
    private BigDecimal deliverySettlementBase;

    @Schema(description = "是否命中满额减免", example = "false")
    private Boolean freeDeliveryEligible;

    @Schema(description = "减免原因 threshold/campaign/manual/none", example = "none")
    private String waiverReason;

    @Schema(description = "使用积分", example = "0")
    private Integer pointUsed;

    @Schema(description = "当前规则下最大可抵扣物业币", example = "50.00")
    private BigDecimal maxCoinAllowed;

    @Schema(description = "实际可采用的物业币抵扣金额", example = "40.00")
    private BigDecimal coinUsed;

    @Schema(description = "现金应付金额", example = "60.00")
    private BigDecimal cashAmount;

    @Schema(description = "若被封顶/限制时的原因说明", example = "超过最大抵扣比例")
    private String reasonIfCapped;

    @Schema(description = "社区食堂可用定向余额列表（按主商家体系，余额>0才返回；消费时需选择体系抵扣）")
    private List<DirectedBalanceResponse> directedBalances;
}
