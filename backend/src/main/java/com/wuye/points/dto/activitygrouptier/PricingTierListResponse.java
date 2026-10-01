package com.wuye.points.dto.activitygrouptier;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "活动组课时价格级别列表响应")
public class PricingTierListResponse {

    @Schema(description = "活动组ID", example = "act_xxxxxxxx")
    private String activityGroupId;

    @Schema(description = "价格级别列表")
    private List<PricingTierItemResponse> list;
}
