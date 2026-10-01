package com.wuye.points.dto.coordinator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "统筹新增板块负责人请求")
public class AddSectorLeaderRequest {

    @NotBlank(message = "residentId不能为空")
    @Schema(description = "业主ID", example = "res_xxxx", requiredMode = Schema.RequiredMode.REQUIRED)
    private String residentId;

    @NotBlank(message = "sector不能为空")
    @Schema(description = "板块编码", example = "cleaning", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sector;

    @Schema(description = "抽佣比例", example = "0.1000")
    private BigDecimal commissionRate;
}
