package com.wuye.points.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "当前用户有效权限")
public class MyPermissionsResponse {

    @Schema(description = "库内原始角色", example = "resident")
    private String role;

    @Schema(description = "归一化角色（历史物业角色已归一为 property_admin）", example = "property_admin")
    private String normalizedRole;

    @Schema(description = "物业子角色", example = "property_operator")
    private String propertySubRole;

    @Schema(description = "物业公司ID")
    private String propertyCompanyId;

    @Schema(description = "是否具备管理端工作台身份（平台/物业/社区管理员，或持组长/负责人标签的住户）", example = "true")
    private Boolean adminWorkspace;

    @Schema(description = "有效角色码集合", example = "[\"resident\",\"coordinator\"]")
    private List<String> roleCodes;

    @Schema(description = "有效权限码（冒号格式）", example = "[\"merchant:audit\",\"distribution:read\"]")
    private List<String> permissions;
}
