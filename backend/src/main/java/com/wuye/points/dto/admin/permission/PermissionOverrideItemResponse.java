package com.wuye.points.dto.admin.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "权限覆盖项响应")
public class PermissionOverrideItemResponse {
    @Schema(description = "权限编码", example = "permission.user.view")
    private String permissionCode;

    @Schema(description = "是否授权", example = "true")
    private Boolean granted;

    @Schema(description = "来源", example = "PRESET")
    private String source;

    @Schema(description = "操作人ID", example = "ADMIN001")
    private String operatorId;

    @Schema(description = "更新时间", example = "2024-01-01 12:00:00")
    private String updatedAt;
}
