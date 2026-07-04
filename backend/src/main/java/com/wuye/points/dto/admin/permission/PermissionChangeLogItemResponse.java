package com.wuye.points.dto.admin.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "权限变更日志项响应")
public class PermissionChangeLogItemResponse {
    @Schema(description = "日志ID", example = "LOG001")
    private String id;

    @Schema(description = "居民ID", example = "RES001")
    private String residentId;

    @Schema(description = "居民姓名", example = "张三")
    private String residentName;

    @Schema(description = "操作人ID", example = "ADMIN001")
    private String operatorId;

    @Schema(description = "操作人姓名", example = "管理员")
    private String operatorName;

    @Schema(description = "操作类型", example = "GRANT")
    private String action;

    @Schema(description = "权限编码", example = "permission.user.view")
    private String permissionCode;

    @Schema(description = "预设编码", example = "ADMIN_PRESET")
    private String presetCode;

    @Schema(description = "变更前值", example = "false")
    private String beforeValue;

    @Schema(description = "变更后值", example = "true")
    private String afterValue;

    @Schema(description = "变更原因", example = "新员工入职")
    private String reason;

    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private String createdAt;
}
