package com.wuye.points.dto.admin.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "踢出商家请求")
@Data
public class KickMerchantRequest {

    @Schema(description = "踢出原因", example = "违规经营", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "踢出原因不能为空")
    private String reason;
}
