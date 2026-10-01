package com.wuye.points.dto.admin.merchantad;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新广告位平台配置")
public class UpdateMerchantAdSettingsRequest {

    @Schema(description = "广告单日价（元），可选")
    private BigDecimal dayPrice;

    @Schema(description = "免费额度是否启用，可选")
    private Boolean freeQuotaEnabled;

    @Schema(description = "每周免费条数，可选")
    private Integer freeQuotaPerWeek;
}
