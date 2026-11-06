package com.wuye.points.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "已屏蔽邻居项")
@Data
@Builder
public class BlockedNeighborItemResponse {

    @Schema(description = "被屏蔽住户ID", example = "res_xxx")
    private String residentId;

    @Schema(description = "昵称", example = "张三")
    private String name;

    @Schema(description = "头像", example = "https://cdn.example.com/a.png")
    private String avatarUrl;

    @Schema(description = "楼栋", example = "1")
    private String building;

    @Schema(description = "单元", example = "1")
    private String unit;

    @Schema(description = "房号", example = "101")
    private String room;

    @Schema(description = "屏蔽时间", example = "2026-11-05 14:30:00")
    private String blockedAt;
}
