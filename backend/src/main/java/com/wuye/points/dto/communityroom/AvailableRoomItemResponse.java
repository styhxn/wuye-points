package com.wuye.points.dto.communityroom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "可选房号项响应")
public class AvailableRoomItemResponse {

    @Schema(description = "楼栋", example = "1栋")
    private String building;

    @Schema(description = "单元", example = "1单元")
    private String unit;

    @Schema(description = "楼层", example = "1")
    private String floor;

    @Schema(description = "房号", example = "101")
    private String room;

    @Schema(description = "是否已占用（此处固定 false，表示可选）", example = "false")
    private Boolean isOccupied;

    @Schema(description = "已占用住户姓名（可选房号为 null）")
    private String residentName;
}
