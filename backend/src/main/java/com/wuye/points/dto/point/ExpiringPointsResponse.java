package com.wuye.points.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "即将过期积分响应")
public class ExpiringPointsResponse {

    @Schema(description = "即将过期积分", example = "100")
    private Integer expiringPoints;

    @Schema(description = "过期日期", example = "2026-09-01")
    private String expiringDate;
}
