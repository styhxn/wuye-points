package com.wuye.points.dto.admin.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "账户列表项响应")
public class AccountListItemResponse {
    @Schema(description = "账户ID", example = "ACC001")
    private String id;

    @Schema(description = "账户名称", example = "张三")
    private String name;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "角色（库内原始值，活动组长等标签身份仍为 resident）", example = "resident")
    private String role;

    @Schema(description = "归一化角色（历史物业子账号已归一为 property_admin）", example = "property_admin")
    private String normalizedRole;

    @Schema(description = "有效角色码集合（含标签身份，如 resident/activity_leader）",
            example = "[\"resident\",\"activity_leader\"]")
    private List<String> roleCodes;

    @Schema(description = "是否具备管理端工作台身份", example = "true")
    private Boolean adminWorkspace;

    @Schema(description = "物业公司ID", example = "PC001")
    private String propertyCompanyId;

    @Schema(description = "状态", example = "active")
    private String status;

    @Schema(description = "有效权限数量", example = "10")
    private int effectivePermissionCount;
}
