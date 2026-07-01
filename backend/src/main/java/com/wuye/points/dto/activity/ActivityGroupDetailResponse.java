package com.wuye.points.dto.activity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ActivityGroupDetailResponse {

    private String id;
    private String name;
    private String description;
    private String coverUrl;
    private String leaderId;
    private String leaderName;
    private String propertyCompanyId;
    private String communityId;
    private String communityName;
    private Integer memberCount;
    private BigDecimal monthlyFee;
    private BigDecimal yearlyFee;
    private String status;
    private Boolean hasJoined;
    private String myRole;
    private String createdAt;
    private String updatedAt;
}
