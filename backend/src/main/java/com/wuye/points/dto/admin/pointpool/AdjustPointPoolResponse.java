package com.wuye.points.dto.admin.pointpool;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "调整积分池响应")
public class AdjustPointPoolResponse {

    @Schema(description = "记录ID", example = "R001")
    private String id;

    @Schema(description = "积分池类型", example = "PROPERTY", allowableValues = {"PROPERTY", "PLATFORM", "MERCHANT"})
    private String poolType;

    @Schema(description = "调整金额", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "调整后余额", example = "5000.00")
    private BigDecimal balance;

    @Schema(description = "调整说明", example = "业主充值积分")
    private String description;

    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private String createdAt;
}
