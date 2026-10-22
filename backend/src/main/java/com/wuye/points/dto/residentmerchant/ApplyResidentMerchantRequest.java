package com.wuye.points.dto.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "申请业主商户请求")
public class ApplyResidentMerchantRequest {

    @NotNull
    @AssertTrue(message = "须同意协议")
    @Schema(description = "是否同意协议", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean agreedTerms;

    @Schema(description = "小店名称（不传默认为「{业主姓名}的小店」）")
    private String shopName;

    @Schema(description = "店铺简介（可选，对齐普通商家店铺描述）")
    private String description;

    @Schema(description = "营业时间（可选，如 08:00-22:00）")
    private String businessHours;

    @Schema(description = "联系电话（可选，不传则买家看到店主手机号）")
    private String contactPhone;

    @Schema(description = "店铺封面图URL（可选）")
    private String coverUrl;
}
