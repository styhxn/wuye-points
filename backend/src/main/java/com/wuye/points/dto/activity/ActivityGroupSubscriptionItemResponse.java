package com.wuye.points.dto.activity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ActivityGroupSubscriptionItemResponse {

    private String subscriptionId;
    private String groupId;
    private String groupName;
    private String groupCover;
    private String subType;
    private BigDecimal amount;
    private String startDate;
    private String endDate;
    private Integer daysRemaining;
    private Boolean autoRenew;
    private String status;
    private String paidAt;
}
