package com.wuye.points.dto.admin.pointclear;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "执行积分清零请求")
public class RunPointClearRequest {

    @Schema(description = "清零基准日期", example = "2024-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "清零基准日期不能为空")
    private String clearDate;

    @Schema(description = "是否仅预览不实际执行", example = "false")
    private Boolean dryRun;

    @Schema(description = "指定住户ID列表（为空则清零全部）", example = "[\"RES001\", \"RES002\"]")
    private List<String> residentIds;

    @Schema(description = "物业公司ID（为空则清零全部）", example = "COMP001")
    private String propertyCompanyId;
}
