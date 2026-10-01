package com.wuye.points.dto.platformconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "平台配置（归属开关/提现粒度/分成维度）")
public class PlatformConfigResponse {

    @Schema(description = "归属模式 property/platform")
    private String rewardAttributionMode;

    @Schema(description = "提现粒度 per_role/merchant/resident")
    private String withdrawalGranularity;

    @Schema(description = "分成维度 default/simple")
    private String shareDimension;
}
