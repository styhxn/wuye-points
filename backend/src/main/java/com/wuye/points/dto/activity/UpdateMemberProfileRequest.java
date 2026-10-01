package com.wuye.points.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新组员个人简介请求")
public class UpdateMemberProfileRequest {

    @Size(max = 1000, message = "introduction长度不能超过1000")
    @Schema(description = "个人简介（姓名、年龄、擅长领域等）", example = "热爱跑步的程序员")
    private String introduction;
}
