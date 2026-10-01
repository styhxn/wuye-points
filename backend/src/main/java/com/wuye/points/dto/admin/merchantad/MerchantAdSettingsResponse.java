package com.wuye.points.dto.admin.merchantad;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "广告位平台配置")
public class MerchantAdSettingsResponse {

    @Schema(description = "广告单日价（元）")
    private BigDecimal dayPrice;

    @Schema(description = "免费额度是否启用")
    private Boolean freeQuotaEnabled;

    @Schema(description = "每周免费条数")
    private Integer freeQuotaPerWeek;
}
