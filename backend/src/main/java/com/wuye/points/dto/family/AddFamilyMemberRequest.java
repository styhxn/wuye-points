package com.wuye.points.dto.family;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "添加家庭成员请求")
@Data
public class AddFamilyMemberRequest {

    @Schema(description = "手机号", example = "13800138000", required = true)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "姓名", example = "张三")
    @Size(max = 50, message = "姓名长度不能超过50")
    private String name;

    @Schema(description = "关系", example = "配偶", required = true)
    @NotBlank(message = "关系不能为空")
    private String relation;
}
