package com.wuye.points.dto.canteen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 社区食堂-用户定向余额流水项（充值/消费/退款）
 */
@Data
@Builder
@Schema(description = "用户定向余额流水项")
public class DirectedFlowResponse {

    @Schema(description = "流水ID", example = "cdf_xxx")
    private String id;

    @Schema(description = "类型 recharge充值 / consume_deduct消费扣减 / refund_back退款退回", example = "consume_deduct")
    private String flowType;

    @Schema(description = "主商家体系名称", example = "幸福餐饮公司")
    private String mainMerchantName;

    @Schema(description = "发生金额（正数）", example = "20.00")
    private BigDecimal amount;

    @Schema(description = "变动后余额", example = "78.00")
    private BigDecimal balanceAfter;

    @Schema(description = "关联订单号", example = "202608270001")
    private String orderNo;

    @Schema(description = "对方商家名称（消费商家）", example = "麻辣烫窗口")
    private String merchantName;

    @Schema(description = "创建时间（ISO-8601）", example = "2026-08-27T10:00:00.000Z")
    private String createdAt;
}
