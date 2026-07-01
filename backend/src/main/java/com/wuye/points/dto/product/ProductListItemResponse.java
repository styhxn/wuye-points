package com.wuye.points.dto.product;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductListItemResponse {

    private String id;
    private String merchantId;
    private String merchantName;
    private String name;
    private String description;
    private String coverUrl;
    private BigDecimal price;
    private BigDecimal memberPrice;
    private Integer pointPrice;
    private Integer stock;
    private String category;
    private String status;
    private String createdAt;
}
