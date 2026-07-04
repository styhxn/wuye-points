package com.wuye.points.dto.admin.offlinepoint;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "线下积分记录项响应")
public class OfflinePointRecordItemResponse {

    @Schema(description = "记录ID", example = "REC20240101001")
    private String id;

    @Schema(description = "住户ID", example = "RES001")
    private String residentId;

    @Schema(description = "住户姓名", example = "张三")
    private String residentName;

    @Schema(description = "积分数量", example = "100")
    private Integer pointAmount;

    @Schema(description = "操作后积分余额", example = "500")
    private Integer balanceAfter;

    @Schema(description = "积分来源", example = "物业奖励")
    private String source;

    @Schema(description = "积分说明", example = "住户参与社区活动奖励积分")
    private String description;

    @Schema(description = "操作员ID", example = "ADMIN001")
    private String operatorId;

    @Schema(description = "操作员姓名", example = "管理员")
    private String operatorName;

    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private String createdAt;
}
