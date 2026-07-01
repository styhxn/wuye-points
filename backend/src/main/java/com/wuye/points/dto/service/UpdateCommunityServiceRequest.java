package com.wuye.points.dto.service;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateCommunityServiceRequest {

    private String name;
    private String description;
    private List<String> coverUrls;
    private String category;
    private BigDecimal price;
    private BigDecimal memberPrice;
    private String priceUnit;
    private Boolean isSubscription;
    private Integer rankOrder;
    private String status;
}
