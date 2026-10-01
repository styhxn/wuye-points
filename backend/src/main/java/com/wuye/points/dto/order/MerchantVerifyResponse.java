package com.wuye.points.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 商家核实订单响应
 */
@Data
@Builder
@Schema(description = "商家核实订单响应")
public class MerchantVerifyResponse {

    @Schema(description = "订单ID")
    private String orderId;

    @Schema(description = "核实结果：approved/rejected")
    private String verificationResult;

    @Schema(description = "订单状态")
    private String orderStatus;

    @Schema(description = "冻结期结束时间（仅通过时返回）")
    private String freezeEndDate;

    @Schema(description = "操作时间")
    private String verifiedAt;

    @Schema(description = "提示信息")
    private String message;
}
