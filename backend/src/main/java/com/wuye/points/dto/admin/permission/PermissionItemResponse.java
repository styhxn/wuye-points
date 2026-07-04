package com.wuye.points.dto.admin.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "权限项响应")
public class PermissionItemResponse {
    @Schema(description = "权限ID", example = "PERM001")
    private String id;

    @Schema(description = "权限编码", example = "permission.user.view")
    private String code;

    @Schema(description = "权限名称", example = "查看用户")
    private String name;

    @Schema(description = "所属模块", example = "用户管理")
    private String module;

    @Schema(description = "权限描述", example = "允许查看用户列表和详情")
    private String description;

    @Schema(description = "是否仅平台可用", example = "false")
    private Boolean isPlatformOnly;

    @Schema(description = "状态", example = "active")
    private String status;
}
