package com.wuye.points.dto.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "我的业主商户状态")
public class MyResidentMerchantResponse {

    private String applicationId;
    private String status;
    private String statusCode;
    private String visibility;
    private String depositStatus;
    private BigDecimal depositAmount;
    private BigDecimal balance;
    private Long listingCount;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "店铺简介")
    private String description;

    @Schema(description = "营业时间")
    private String businessHours;

    @Schema(description = "联系电话（未设置时为空，前端可回退展示店主手机号）")
    private String contactPhone;

    @Schema(description = "店铺封面图URL")
    private String coverUrl;
}
