package com.wuye.points.dto.admin.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "授权权限响应")
public class GrantPermissionsResponse {
    @Schema(description = "账户ID", example = "ACC001")
    private String accountId;

    @Schema(description = "授权数量", example = "5")
    private Integer grantedCount;

    @Schema(description = "已授权的权限编码列表", example = "[\"permission.user.view\",\"permission.user.edit\"]")
    private List<String> grantedCodes;

    @Schema(description = "操作人ID", example = "ADMIN001")
    private String operatorId;

    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private String createdAt;
}
