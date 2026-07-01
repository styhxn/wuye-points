package com.wuye.points.dto.merchant;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class MerchantProductItemResponse {

    private String id;
    private String name;
    private String description;
    private String coverUrl;
    private BigDecimal price;
    private BigDecimal memberPrice;
    private Integer pointPrice;
    private Integer stock;
    private String category;
    private String status;
}
