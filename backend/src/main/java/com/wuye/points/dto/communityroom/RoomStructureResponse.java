package com.wuye.points.dto.communityroom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "楼宇结构响应")
public class RoomStructureResponse {

    @Schema(description = "社区ID", example = "com_xxxxxxxx")
    private String communityId;

    @Schema(description = "楼栋", example = "1栋")
    private String building;

    @Schema(description = "单元列表")
    private List<UnitStructure> units;

    @Data
    @Builder
    @Schema(description = "单元结构")
    public static class UnitStructure {
        @Schema(description = "单元", example = "1单元")
        private String unit;

        @Schema(description = "楼层列表")
        private List<FloorStructure> floors;
    }

    @Data
    @Builder
    @Schema(description = "楼层结构")
    public static class FloorStructure {
        @Schema(description = "楼层", example = "1")
        private String floor;

        @Schema(description = "房号列表")
        private List<RoomInfo> rooms;
    }

    @Data
    @Builder
    @Schema(description = "房号信息")
    public static class RoomInfo {
        @Schema(description = "房号", example = "101")
        private String room;

        @Schema(description = "是否已占用", example = "true")
        private Boolean isOccupied;

        @Schema(description = "已占用住户姓名")
        private String residentName;

        @Schema(description = "房号状态：vacant 空置 / occupied 已入住 / locked 锁定")
        private String status;
    }
}
