package com.wuye.points.dto.communityroom;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "新增房号请求")
public class AddRoomRequest {

    @Schema(description = "楼栋", example = "1栋")
    @NotBlank(message = "楼栋不能为空")
    private String building;

    @Schema(description = "单元（可空，默认空串）", example = "1单元")
    private String unit;

    @Schema(description = "楼层（可空）", example = "1")
    private String floor;

    @Schema(description = "房号", example = "101")
    @NotBlank(message = "房号不能为空")
    private String room;
}
