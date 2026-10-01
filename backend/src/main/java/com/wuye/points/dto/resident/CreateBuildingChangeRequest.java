package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "楼栋变更申请请求")
@Data
public class CreateBuildingChangeRequest {

    @Schema(description = "目标小区ID", example = "com_def456", required = true)
    @NotBlank(message = "小区ID不能为空")
    private String communityId;

    @Schema(description = "目标楼栋", example = "3", required = true)
    @NotBlank(message = "楼栋不能为空")
    @Size(max = 20, message = "楼栋长度不能超过20")
    private String building;

    @Schema(description = "目标单元", example = "2")
    @Size(max = 20, message = "单元长度不能超过20")
    private String unit;

    @Schema(description = "目标楼层", example = "5")
    @Size(max = 20, message = "楼层长度不能超过20")
    private String floor;

    @Schema(description = "目标房号", example = "501", required = true)
    @NotBlank(message = "房号不能为空")
    @Size(max = 20, message = "房号长度不能超过20")
    private String room;
}
