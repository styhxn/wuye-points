package com.wuye.points.dto.admin.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "角色预设列表响应")
public class RolePresetListResponse {
    @Schema(description = "角色预设列表", example = "[]")
    private List<RolePresetItemResponse> presets;
}
