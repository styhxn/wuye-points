package com.wuye.points.dto.communityroom;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量新增房号请求")
public class AddRoomsBatchRequest {

    @Schema(description = "楼栋", example = "1栋")
    @NotBlank(message = "楼栋不能为空")
    private String building;

    @Schema(description = "单元（可空，作用于本批次全部房号）", example = "1单元")
    private String unit;

    @Schema(description = "房号列表")
    @NotNull(message = "房号列表不能为空")
    private List<RoomItem> rooms;

    @Data
    @Schema(description = "房号项")
    public static class RoomItem {
        @Schema(description = "楼层（可空）", example = "1")
        private String floor;

        @Schema(description = "房号", example = "101")
        @NotBlank(message = "房号不能为空")
        private String room;
    }
}
