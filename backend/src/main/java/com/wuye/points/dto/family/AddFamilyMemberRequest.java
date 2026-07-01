package com.wuye.points.dto.family;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddFamilyMemberRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @Size(max = 50, message = "姓名长度不能超过50")
    private String name;

    @NotBlank(message = "关系不能为空")
    private String relation;
}
