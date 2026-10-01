package com.wuye.points.dto.servicerequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "服务需求商家队列项")
public class ServiceRequestQueueItemResponse {

    private String queueId;
    private String merchantId;
    private String merchantName;
    private String status;
    private String statusCode;
    private Integer queueOrder;
    private String notifiedAt;
    private String respondedAt;
}
