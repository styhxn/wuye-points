package com.wuye.points.dto.servicerequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "商家待响应服务需求")
public class MerchantPendingServiceRequestResponse {

    @Schema(description = "队列项ID")
    private String queueId;

    @Schema(description = "需求ID")
    private String requestId;

    @Schema(description = "需求描述")
    private String description;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "服务地址")
    private String address;

    @Schema(description = "匹配分类名称")
    private String matchedCategoryName;

    @Schema(description = "状态（中文）")
    private String status;

    @Schema(description = "状态码")
    private String statusCode;

    @Schema(description = "超时时间")
    private String timeoutAt;

    @Schema(description = "通知时间")
    private String notifiedAt;
}
