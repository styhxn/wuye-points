package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "住户状态响应")
public class ResidentStatusResponse {

    @Schema(description = "住户ID", example = "res_b7m9x1")
    private String id;

    @Schema(description = "住户状态", example = "active", allowableValues = {"active", "frozen", "disabled"})
    private String status;

    @Schema(description = "更新时间", example = "2026-01-01T00:00:00.000Z")
    private String updatedAt;
}
