package com.wuye.points.dto.admin.roleaccount;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 禁用/停用业务角色账号响应
 */
@Data
@Builder
@Schema(description = "禁用/停用业务角色账号响应")
public class DisableRoleAccountResponse {

    @Schema(description = "账号ID", example = "res_mch08")
    private String residentId;

    @Schema(description = "账号状态，固定 disabled", example = "disabled")
    private String status;

    @Schema(description = "关联店铺最终状态：none=无店铺；stopped=已停用；kicked/rejected 等=原有状态（未改动）", example = "stopped")
    private String merchantStatus;

    @Schema(description = "操作说明", example = "账号已禁用（关联店铺已停用），历史业务数据完整保留")
    private String message;
}
