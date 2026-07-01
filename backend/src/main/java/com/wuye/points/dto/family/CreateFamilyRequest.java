package com.wuye.points.dto.family;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateFamilyRequest {

    @NotBlank(message = "家庭名称不能为空")
    @Size(min = 1, max = 50, message = "家庭名称长度需在1-50之间")
    private String name;

    @NotBlank(message = "楼栋不能为空")
    @Size(max = 20, message = "楼栋长度不能超过20")
    private String building;

    @Size(max = 20, message = "单元长度不能超过20")
    private String unit;

    @NotBlank(message = "房号不能为空")
    @Size(max = 20, message = "房号长度不能超过20")
    private String room;

    @NotBlank(message = "小区ID不能为空")
    private String communityId;
}
