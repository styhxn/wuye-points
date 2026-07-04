package com.wuye.points.dto.admin.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "授权权限请求")
public class GrantPermissionsRequest {

    @NotEmpty(message = "permissionCodes不能为空")
    @Schema(description = "要授权的权限编码列表", example = "[\"permission.user.view\",\"permission.user.edit\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> permissionCodes;

    @Schema(description = "授权原因", example = "新员工入职需要基础权限")
    private String reason;
}
