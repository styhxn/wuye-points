package com.wuye.points.dto.admin.roleaccount;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 直接删除业务角色账号响应
 */
@Data
@Builder
@Schema(description = "直接删除业务角色账号响应")
public class DeleteRoleAccountResponse {

    @Schema(description = "账号ID", example = "res_mch08")
    private String residentId;

    @Schema(description = "是否已删除", example = "true")
    private Boolean deleted;

    @Schema(description = "删除方式说明", example = "账号已彻底删除（含关联店铺）")
    private String message;
}
