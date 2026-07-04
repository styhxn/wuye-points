package com.wuye.points.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "积分记录明细响应")
public class PointRecordItemResponse {

    @Schema(description = "记录ID", example = "pr_abc123")
    private String id;

    @Schema(description = "用户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "家庭ID", example = "fam_abc123")
    private String familyId;

    @Schema(description = "积分类型", example = "earn")
    private String pointType;

    @Schema(description = "积分数量", example = "50")
    private Integer amount;

    @Schema(description = "操作后余额", example = "450")
    private Integer balance;

    @Schema(description = "来源", example = "签到")
    private String source;

    @Schema(description = "来源ID", example = "sign_abc123")
    private String sourceId;

    @Schema(description = "描述", example = "每日签到获得积分")
    private String description;

    @Schema(description = "操作人ID", example = "res_b7m9x1")
    private String operatorId;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;
}
