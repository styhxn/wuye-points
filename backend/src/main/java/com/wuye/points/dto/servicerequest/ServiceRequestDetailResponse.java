package com.wuye.points.dto.servicerequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "服务需求详情")
public class ServiceRequestDetailResponse {

    private String id;
    private String description;
    private String status;
    private String statusCode;
    private String matchedCategoryName;
    private String currentMerchantName;
    private String contactPhone;
    private String address;
    private String preferredTime;
    private BigDecimal quoteAmount;
    private String quotePaymentMethod;
    private String closedReason;
    private String createdAt;
    private List<ServiceRequestQueueItemResponse> queue;
    private ServiceRequestQuoteInfo quote;
}
