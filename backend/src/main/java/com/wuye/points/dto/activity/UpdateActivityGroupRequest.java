package com.wuye.points.dto.activity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateActivityGroupRequest {

    private String name;
    private String description;
    private String coverUrl;
    private String communityId;
    private BigDecimal monthlyFee;
    private BigDecimal yearlyFee;
    private String status;
}
