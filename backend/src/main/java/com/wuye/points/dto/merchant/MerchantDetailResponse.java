package com.wuye.points.dto.merchant;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MerchantDetailResponse {

    private String id;
    private String platformMerchantId;
    private String name;
    private String description;
    private List<String> coverUrls;
    private String videoUrl;
    private String qrCodeUrl;
    private String merchantLevel;
    private Integer levelWeight;
    private String category;
    private String businessHours;
    private String contactPhone;
    private String address;
    private java.math.BigDecimal deliveryFee;
    private java.math.BigDecimal freeDeliveryThreshold;
    private java.math.BigDecimal memberDiscountPrice;
    private java.math.BigDecimal coinRebateRate;
    private Boolean coinRebateEnabled;
    private Integer rankOrder;
    private String auditStatus;
    private String status;
    private String createdAt;
    private String updatedAt;
    private List<MerchantProductItemResponse> products;
}
