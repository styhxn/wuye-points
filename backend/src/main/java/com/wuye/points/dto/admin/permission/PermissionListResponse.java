package com.wuye.points.dto.admin.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "权限列表响应")
public class PermissionListResponse {
    @Schema(description = "权限列表", example = "[]")
    private List<PermissionItemResponse> permissions;

    @Schema(description = "模块列表", example = "[\"用户管理\",\"订单管理\"]")
    private List<String> modules;
}
