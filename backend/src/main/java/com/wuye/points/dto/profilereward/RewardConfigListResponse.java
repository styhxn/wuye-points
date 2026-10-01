package com.wuye.points.dto.profilereward;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "信息采集奖励配置列表响应")
public class RewardConfigListResponse {

    @Schema(description = "配置列表")
    private List<RewardConfigItemResponse> list;

    @Schema(description = "总数", example = "5")
    private long total;
}
