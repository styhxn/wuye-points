package com.wuye.points.dto.admin.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "已应用预设响应")
public class AppliedPresetResponse {
    @Schema(description = "预设ID", example = "RP001")
    private String id;

    @Schema(description = "预设名称", example = "管理员预设")
    private String name;

    @Schema(description = "预设编码", example = "ADMIN_PRESET")
    private String code;
}
