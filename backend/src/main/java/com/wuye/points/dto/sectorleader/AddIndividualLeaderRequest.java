package com.wuye.points.dto.sectorleader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "板块负责人新增个体负责人请求")
public class AddIndividualLeaderRequest {

    @NotBlank(message = "residentId不能为空")
    @Schema(description = "业主ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "res_xxx")
    private String residentId;

    @NotBlank(message = "sector不能为空")
    @Schema(description = "所属板块", requiredMode = Schema.RequiredMode.REQUIRED, example = "cleaning")
    private String sector;

    @Schema(description = "个体负责人姓名", example = "李四")
    private String name;

    @Schema(description = "分成比例", example = "0.03")
    private BigDecimal commissionRate;
}
