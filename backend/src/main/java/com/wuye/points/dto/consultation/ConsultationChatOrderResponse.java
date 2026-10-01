package com.wuye.points.dto.consultation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "图文咨询订单响应")
public class ConsultationChatOrderResponse {

    private String orderId;
    private String conversationPeerId;
}
