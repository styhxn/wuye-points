package com.wuye.points.dto.inquiry;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "发起询价请求")
public class CreateInquiryRequest {

    @NotBlank(message = "merchantId不能为空")
    @Schema(description = "商家ID", example = "mch_xxxx", requiredMode = Schema.RequiredMode.REQUIRED)
    private String merchantId;

    @Schema(description = "商品ID", example = "p_xxxx")
    private String productId;

    @Schema(description = "商品名称", example = "示例商品")
    private String productName;

    @NotBlank(message = "content不能为空")
    @Schema(description = "询价内容", example = "请问批发价多少？", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}
