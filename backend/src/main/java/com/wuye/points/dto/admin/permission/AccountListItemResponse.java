package com.wuye.points.dto.admin.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

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

    @Schema(description = "角色", example = "admin")
    private String role;

    @Schema(description = "物业公司ID", example = "PC001")
    private String propertyCompanyId;

    @Schema(description = "状态", example = "active")
    private String status;

    @Schema(description = "有效权限数量", example = "10")
    private int effectivePermissionCount;
}
