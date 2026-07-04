package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新住户信息请求")
public class UpdateResidentRequest {

    @Size(max = 50, message = "姓名长度不能超过50")
    @Schema(description = "姓名", example = "张三")
    private String name;

    @Size(max = 500, message = "头像URL长度不能超过500")
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Min(value = 0, message = "性别值无效")
    @Max(value = 2, message = "性别值无效")
    @Schema(description = "性别（0未知 1男 2女）", example = "1")
    private Integer gender;

    @Min(value = 0, message = "年龄不能小于0")
    @Max(value = 150, message = "年龄不能大于150")
    @Schema(description = "年龄", example = "30")
    private Integer age;

    @Size(max = 20, message = "婚姻状态无效")
    @Schema(description = "婚姻状态", example = "已婚")
    private String maritalStatus;

    @Schema(description = "是否有子女", example = "true")
    private Boolean hasChildren;

    @Size(max = 20, message = "楼栋长度不能超过20")
    @Schema(description = "楼栋", example = "1栋")
    private String building;

    @Size(max = 20, message = "单元长度不能超过20")
    @Schema(description = "单元", example = "1单元")
    private String unit;

    @Size(max = 20, message = "房号长度不能超过20")
    @Schema(description = "房号", example = "101")
    private String room;

    @Schema(description = "角色", example = "user")
    private String role;
}
