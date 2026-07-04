package com.wuye.points.dto.admin.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "用户权限响应")
public class UserPermissionsResponse {
    @Schema(description = "账户ID", example = "ACC001")
    private String accountId;

    @Schema(description = "账户名称", example = "张三")
    private String accountName;

    @Schema(description = "已应用预设")
    private AppliedPresetResponse appliedPreset;

    @Schema(description = "权限覆盖列表")
    private List<PermissionOverrideItemResponse> overrides;

    @Schema(description = "有效权限列表", example = "[\"permission.user.view\",\"permission.user.edit\"]")
    private List<String> effectivePermissions;

    @Schema(description = "有效权限数量", example = "10")
    private Integer effectivePermissionCount;

    @Schema(description = "更新时间", example = "2024-01-01 12:00:00")
    private String updatedAt;
}
