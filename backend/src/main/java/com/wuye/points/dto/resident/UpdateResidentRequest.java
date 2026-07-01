package com.wuye.points.dto.resident;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateResidentRequest {

    @Size(max = 50, message = "姓名长度不能超过50")
    private String name;

    @Size(max = 500, message = "头像URL长度不能超过500")
    private String avatarUrl;

    @Min(value = 0, message = "性别值无效")
    @Max(value = 2, message = "性别值无效")
    private Integer gender;

    @Min(value = 0, message = "年龄不能小于0")
    @Max(value = 150, message = "年龄不能大于150")
    private Integer age;

    @Size(max = 20, message = "婚姻状态无效")
    private String maritalStatus;

    private Boolean hasChildren;

    @Size(max = 20, message = "楼栋长度不能超过20")
    private String building;

    @Size(max = 20, message = "单元长度不能超过20")
    private String unit;

    @Size(max = 20, message = "房号长度不能超过20")
    private String room;
}
