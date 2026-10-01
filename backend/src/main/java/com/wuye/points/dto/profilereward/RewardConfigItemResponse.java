package com.wuye.points.dto.profilereward;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "信息采集奖励配置项响应")
public class RewardConfigItemResponse {

    @Schema(description = "配置ID", example = "pcr_abc12345")
    private String id;

    @Schema(description = "作用域", example = "property_company",
            allowableValues = {"property_company", "community", "platform", "coordinator"})
    private String scope;

    @Schema(description = "作用域ID", example = "pc_abc12345")
    private String scopeId;

    @Schema(description = "字段名", example = "birthday")
    private String fieldName;

    @Schema(description = "奖励积分数", example = "10")
    private Integer rewardPoints;

    @Schema(description = "是否每用户仅奖励一次", example = "true")
    private Boolean oncePerUser;

    @Schema(description = "状态", example = "active", allowableValues = {"active", "inactive"})
    private String status;

    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private String createdAt;

    @Schema(description = "更新时间", example = "2024-01-01 10:00:00")
    private String updatedAt;
}
