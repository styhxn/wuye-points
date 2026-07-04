package com.wuye.points.dto.admin.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "创建角色预设请求")
public class CreateRolePresetRequest {

    @NotBlank(message = "预设名称不能为空")
    @Schema(description = "预设名称", example = "管理员预设", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "预设编码", example = "ADMIN_PRESET")
    private String code;

    @Schema(description = "预设描述", example = "管理员角色预设，包含所有管理权限")
    private String description;

    @NotEmpty(message = "permissionCodes不能为空")
    @Schema(description = "权限编码列表", example = "[\"permission.user.view\",\"permission.user.edit\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> permissionCodes;
}
