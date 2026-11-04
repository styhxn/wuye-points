package com.wuye.points.dto.propertycompany;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "物业币显示开关响应")
public class CoinDisplaySettingResponse {

    @Schema(description = "物业公司ID", example = "pc_001")
    private String propertyCompanyId;

    @Schema(description = "是否对住户显示物业币", example = "true")
    private Boolean coinDisplayEnabled;

    @Schema(description = "更新时间", example = "2026-10-29T12:00:00Z")
    private String updatedAt;
}
