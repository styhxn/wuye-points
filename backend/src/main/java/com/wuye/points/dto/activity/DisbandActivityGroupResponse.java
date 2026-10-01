package com.wuye.points.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "解散（硬删除）活动组响应")
public class DisbandActivityGroupResponse {

    @Schema(description = "被解散的活动组ID", example = "act_8db01000")
    private String groupId;

    @Schema(description = "结果状态，固定为 deleted", example = "deleted")
    private String status;
}
