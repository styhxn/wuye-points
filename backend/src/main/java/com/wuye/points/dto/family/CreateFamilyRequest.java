package com.wuye.points.dto.family;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "创建家庭请求")
@Data
public class CreateFamilyRequest {

    @Schema(description = "家庭名称", example = "幸福之家", required = true)
    @NotBlank(message = "家庭名称不能为空")
    @Size(min = 1, max = 50, message = "家庭名称长度需在1-50之间")
    private String name;

    @Schema(description = "楼栋", example = "1栋", required = true)
    @NotBlank(message = "楼栋不能为空")
    @Size(max = 20, message = "楼栋长度不能超过20")
    private String building;

    @Schema(description = "单元", example = "1单元")
    @Size(max = 20, message = "单元长度不能超过20")
    private String unit;

    @Schema(description = "房号", example = "101", required = true)
    @NotBlank(message = "房号不能为空")
    @Size(max = 20, message = "房号长度不能超过20")
    private String room;

    @Schema(description = "小区ID", example = "com_xxx", required = true)
    @NotBlank(message = "小区ID不能为空")
    private String communityId;
}
