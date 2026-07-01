package com.wuye.points.dto.service;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateCommunityServiceRequest {

    @NotBlank(message = "服务名称不能为空")
    private String name;

    private String description;

    private List<String> coverUrls;

    @NotBlank(message = "category不能为空")
    private String category;

    private BigDecimal price;

    private BigDecimal memberPrice;

    private String priceUnit;

    private Boolean isSubscription;

    private Integer rankOrder;
}
