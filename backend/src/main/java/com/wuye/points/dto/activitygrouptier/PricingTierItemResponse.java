package com.wuye.points.dto.activitygrouptier;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "活动组课时价格级别项响应")
public class PricingTierItemResponse {

    @Schema(description = "级别ID", example = "agt_xxxxxxxx")
    private String id;

    @Schema(description = "活动组ID", example = "act_xxxxxxxx")
    private String activityGroupId;

    @Schema(description = "级别名称", example = "一级")
    private String tierName;

    @Schema(description = "级别编码", example = "level_1")
    private String tierCode;

    @Schema(description = "价格", example = "100.00")
    private BigDecimal price;

    @Schema(description = "周期", example = "monthly")
    private String period;

    @Schema(description = "排序", example = "0")
    private Integer sortOrder;

    @Schema(description = "状态", example = "active")
    private String status;

    @Schema(description = "创建时间", example = "2024-01-01T00:00:00")
    private String createdAt;

    @Schema(description = "更新时间", example = "2024-01-01T00:00:00")
    private String updatedAt;
}
