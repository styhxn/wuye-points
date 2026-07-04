package com.wuye.points.dto.admin.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "角色预设项响应")
public class RolePresetItemResponse {
    @Schema(description = "预设ID", example = "RP001")
    private String id;

    @Schema(description = "预设名称", example = "管理员预设")
    private String name;

    @Schema(description = "预设编码", example = "ADMIN_PRESET")
    private String code;

    @Schema(description = "预设描述", example = "管理员角色预设，包含所有管理权限")
    private String description;

    @Schema(description = "物业公司ID", example = "PC001")
    private String propertyCompanyId;

    @Schema(description = "是否内置预设", example = "false")
    private Boolean isBuiltin;

    @Schema(description = "权限编码列表", example = "[\"permission.user.view\",\"permission.user.edit\"]")
    private List<String> permissionCodes;

    @Schema(description = "权限数量", example = "10")
    private Integer permissionCount;

    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private String createdAt;
}
