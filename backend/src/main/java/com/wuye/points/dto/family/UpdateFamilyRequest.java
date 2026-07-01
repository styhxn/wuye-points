package com.wuye.points.dto.family;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateFamilyRequest {

    @Size(min = 1, max = 50, message = "家庭名称长度需在1-50之间")
    private String name;

    @Size(max = 20, message = "楼栋长度不能超过20")
    private String building;

    @Size(max = 20, message = "单元长度不能超过20")
    private String unit;

    @Size(max = 20, message = "房号长度不能超过20")
    private String room;
}
