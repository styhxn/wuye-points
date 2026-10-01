package com.wuye.points.dto.coordinator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "统筹新增个体负责人请求")
public class AddIndividualLeaderRequest {

    @NotBlank(message = "residentId不能为空")
    @Schema(description = "业主ID", example = "res_xxxx", requiredMode = Schema.RequiredMode.REQUIRED)
    private String residentId;

    @NotBlank(message = "sector不能为空")
    @Schema(description = "板块编码", example = "cleaning", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sector;

    @Schema(description = "个体负责人名称", example = "张三")
    private String name;

    @NotBlank(message = "sectorLeaderId不能为空")
    @Schema(description = "所属板块负责人ID", example = "sl_xxxx", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sectorLeaderId;

    @Schema(description = "抽佣比例", example = "0.0500")
    private BigDecimal commissionRate;
}
