package com.wuye.points.dto.communityroom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "房号管理项")
public class RoomManageItemResponse {

    @Schema(description = "房号ID", example = "room_xxxxxxxx")
    private String id;

    @Schema(description = "社区ID", example = "com_xxxxxxxx")
    private String communityId;

    @Schema(description = "楼栋", example = "1栋")
    private String building;

    @Schema(description = "单元", example = "1单元")
    private String unit;

    @Schema(description = "楼层", example = "1")
    private String floor;

    @Schema(description = "房号", example = "101")
    private String room;

    @Schema(description = "状态：vacant 空置 / occupied 已入住 / locked 锁定")
    private String status;

    @Schema(description = "当前入住住户ID")
    private String residentId;

    @Schema(description = "当前入住住户姓名")
    private String residentName;

    @Schema(description = "创建时间(ISO8601)")
    private String createdAt;
}
