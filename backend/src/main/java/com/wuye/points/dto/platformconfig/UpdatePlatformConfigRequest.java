package com.wuye.points.dto.platformconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新平台配置（仅更新非 null 字段）")
public class UpdatePlatformConfigRequest {

    @Schema(description = "归属模式 property/platform")
    private String rewardAttributionMode;

    @Schema(description = "提现粒度 per_role/merchant/resident")
    private String withdrawalGranularity;

    @Schema(description = "分成维度 default/simple")
    private String shareDimension;
}
