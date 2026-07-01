package com.wuye.points.dto.propertycompany;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PropertyCompanyDetailResponse {

    private String id;
    private String name;
    private String logoUrl;
    private String contactPhone;
    private String address;
    private String status;
    private BigDecimal propertyShareRate;
    private BigDecimal coordinatorShareRate;
    private BigDecimal sectorLeaderRate;
    private BigDecimal individualLeaderRate;
    private Boolean coinDisplayEnabled;
    private String coinIssueMode;
    private Integer coinExpiryDays;
    private Boolean coinFreezeDefault;
    private BigDecimal deliveryBaseFee;
    private BigDecimal deliveryCourierShareRate;
    private BigDecimal deliveryCourierPerOrder;
    private BigDecimal withdrawalFeeRate;
    private BigDecimal pointToFeeRate;
    private Boolean twoYearClearEnabled;
    private Integer neighborDailyContactLimit;
    private List<CommunityItemResponse> communities;
    private List<AdminAccountResponse> adminAccounts;
    private String createdAt;
    private String updatedAt;
}
