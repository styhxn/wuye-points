package com.wuye.points.dto.admin.coinexpiry;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "执行积分过期请求")
public class RunCoinExpiryRequest {

    @NotBlank(message = "过期日期不能为空")
    @Schema(description = "过期日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-01-01")
    private String expiryDate;

    @Schema(description = "是否试运行", example = "false")
    private Boolean dryRun;

    @Schema(description = "指定住户ID列表", example = "[\"RES001\", \"RES002\"]")
    private List<String> residentIds;

    @Schema(description = "物业公司ID", example = "PROP001")
    private String propertyCompanyId;
}
