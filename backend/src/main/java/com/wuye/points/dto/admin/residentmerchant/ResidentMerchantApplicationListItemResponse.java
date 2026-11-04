package com.wuye.points.dto.admin.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "业主商户申请列表项")
public class ResidentMerchantApplicationListItemResponse {

    private String id;
    private String residentId;
    private String residentName;
    private String propertyCompanyId;
    private BigDecimal depositAmount;
    private String status;
    private String statusCode;
    private String visibility;
    private String createdAt;

    @Schema(description = "店铺名称", example = "张三的小店")
    private String shopName;

    @Schema(description = "店铺简介", example = "小区内新鲜水果代购")
    private String description;

    @Schema(description = "营业时间", example = "08:00-22:00")
    private String businessHours;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "店铺封面图URL", example = "https://cdn.example.com/shop/cover.jpg")
    private String coverUrl;
}
