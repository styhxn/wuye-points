package com.wuye.points.dto.admin.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "撤销权限响应")
public class RevokePermissionsResponse {
    @Schema(description = "账户ID", example = "ACC001")
    private String accountId;

    @Schema(description = "撤销数量", example = "3")
    private Integer revokedCount;

    @Schema(description = "已撤销的权限编码列表", example = "[\"permission.user.view\",\"permission.user.edit\"]")
    private List<String> revokedCodes;

    @Schema(description = "操作人ID", example = "ADMIN001")
    private String operatorId;

    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private String createdAt;
}
