package com.wuye.points.dto.coordinator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "商户冻结/解冻请求")
public class FreezeMerchantRequest {

    @NotNull(message = "frozen不能为空")
    @Schema(description = "是否冻结", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean frozen;
}
