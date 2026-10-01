package com.wuye.points.dto.admin.roleaccount;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 禁用/停用业务角色账号请求
 */
@Data
@Schema(description = "禁用/停用业务角色账号请求")
public class DisableRoleAccountRequest {

    @Schema(description = "禁用原因（操作审计）", example = "商家已被踢出且存在业务数据，停用账号")
    @NotBlank(message = "禁用原因不能为空")
    @Size(max = 200, message = "禁用原因长度不能超过200")
    private String reason;
}
