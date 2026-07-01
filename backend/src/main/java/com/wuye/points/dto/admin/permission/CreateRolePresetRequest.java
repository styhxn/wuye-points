package com.wuye.points.dto.admin.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateRolePresetRequest {

    @NotBlank(message = "预设名称不能为空")
    private String name;

    private String code;

    private String description;

    @NotEmpty(message = "permissionCodes不能为空")
    private List<String> permissionCodes;
}
