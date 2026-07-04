package com.wuye.points.dto.admin.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "更新角色预设请求")
public class UpdateRolePresetRequest {
    @Schema(description = "预设名称", example = "管理员预设")
    private String name;

    @Schema(description = "预设编码", example = "ADMIN_PRESET")
    private String code;

    @Schema(description = "预设描述", example = "管理员角色预设，包含所有管理权限")
    private String description;

    @Schema(description = "权限编码列表", example = "[\"permission.user.view\",\"permission.user.edit\"]")
    private List<String> permissionCodes;
}
