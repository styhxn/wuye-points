package com.wuye.points.dto.sectorleader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "板块负责人上传商家请求")
public class UploadMerchantRequest {

    @NotBlank(message = "platformMerchantId不能为空")
    @Schema(description = "平台商家ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "pm_xxx")
    private String platformMerchantId;

    @NotBlank(message = "name不能为空")
    @Schema(description = "商家名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "示例商家")
    private String name;

    @Schema(description = "商家分类", example = "外卖")
    private String category;

    @Schema(description = "联系电话", example = "13800000000")
    private String contactPhone;

    @Schema(description = "地址", example = "示例地址")
    private String address;
}
