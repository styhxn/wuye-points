package com.wuye.points.dto.priceapproval;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "提交价格修改审批请求")
public class CreatePriceApprovalRequest {

    @Schema(description = "物业公司ID（仅 platform_admin 提交时必填，pc_ 开头）", example = "pc_demo001")
    private String propertyCompanyId;

    @NotBlank(message = "itemType不能为空")
    @Pattern(regexp = "delivery_fee|product_price|merchant_ad|merchant_distribution|property_fee_price|resident_share_rate",
            message = "itemType 必须为已知枚举值(delivery_fee/product_price/merchant_ad/merchant_distribution/property_fee_price/resident_share_rate)")
    @Schema(description = "审批类型: delivery_fee/product_price/merchant_ad/merchant_distribution/property_fee_price/resident_share_rate",
            example = "product_price", requiredMode = Schema.RequiredMode.REQUIRED)
    private String itemType;

    @Schema(description = "目标对象ID", example = "p_xxxx")
    private String itemId;

    @Schema(description = "原值", example = "10.00")
    private String oldValue;

    @NotBlank(message = "newValue不能为空")
    @Schema(description = "新值", example = "12.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newValue;

    @Schema(description = "原因", example = "原材料价格上涨")
    private String reason;
}
