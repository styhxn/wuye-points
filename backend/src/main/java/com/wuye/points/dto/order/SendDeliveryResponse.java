package com.wuye.points.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "商家发送配送任务响应")
public class SendDeliveryResponse {

    @Schema(description = "订单ID")
    private String orderId;

    @Schema(description = "配送单ID")
    private String deliveryId;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "状态 code（逻辑判断用）")
    private String statusCode;

    @Schema(description = "状态中文（展示用，与 status 相同）")
    private String statusLabel;

    @Schema(description = "是否复用已有配送单（幂等）", example = "false")
    private Boolean alreadyExists;

    @Schema(description = "发送时间")
    private String sentAt;
}
