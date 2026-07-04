package com.wuye.points.dto.admin.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "撤销权限请求")
public class RevokePermissionsRequest {

    @NotEmpty(message = "permissionCodes不能为空")
    @Schema(description = "要撤销的权限编码列表", example = "[\"permission.user.view\",\"permission.user.edit\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> permissionCodes;

    @Schema(description = "撤销原因", example = "员工离职")
    private String reason;
}
