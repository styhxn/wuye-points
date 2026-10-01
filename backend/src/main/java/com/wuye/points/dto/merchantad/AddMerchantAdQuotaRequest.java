package com.wuye.points.dto.merchantad;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "增加商家广告周额度")
public class AddMerchantAdQuotaRequest {

    @NotNull
    @Schema(description = "增加的付费额度")
    private Integer purchasedQuota;
}
