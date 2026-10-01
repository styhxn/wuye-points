package com.wuye.points.dto.sectorleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "板块负责人上传/审核商家响应")
public class MerchantUploadResponse {

    @Schema(description = "商家ID")
    private String id;

    @Schema(description = "平台商家ID")
    private String platformMerchantId;

    @Schema(description = "物业公司ID")
    private String propertyCompanyId;

    @Schema(description = "商家名称")
    private String name;

    @Schema(description = "商家分类")
    private String category;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "配送费")
    private BigDecimal deliveryFee;

    @Schema(description = "满额免配送费")
    private BigDecimal freeDeliveryThreshold;

    @Schema(description = "审核状态")
    private String auditStatus;

    @Schema(description = "状态")
    private String status;
}
