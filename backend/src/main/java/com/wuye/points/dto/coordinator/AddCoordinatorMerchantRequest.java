package com.wuye.points.dto.coordinator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "统筹新增商户请求")
public class AddCoordinatorMerchantRequest {

    @NotBlank(message = "platformMerchantId不能为空")
    @Schema(description = "平台商家ID", example = "pm_xxxx", requiredMode = Schema.RequiredMode.REQUIRED)
    private String platformMerchantId;

    @NotBlank(message = "name不能为空")
    @Schema(description = "商家名称", example = "示例商家", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "商家分类", example = "外卖")
    private String category;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "地址", example = "示例路1号")
    private String address;
}
