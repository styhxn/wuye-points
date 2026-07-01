package com.wuye.points.dto.service;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CommunityServiceListItemResponse {

    private String id;
    private String name;
    private String description;
    private List<String> coverUrls;
    private String category;
    private BigDecimal price;
    private BigDecimal memberPrice;
    private String priceUnit;
    private String providerId;
    private String providerName;
    private String providerType;
    private Boolean isSubscription;
    private Integer rankOrder;
    private String status;
    private String propertyCompanyId;
    private String createdAt;
}
