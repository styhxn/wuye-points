package com.wuye.points.dto.admin.propertymanager;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "指派物业经理请求")
public class CreatePropertyManagerRequest {

    @NotBlank(message = "residentId不能为空")
    @Schema(description = "经理对应住户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "res_xxx")
    private String residentId;

    @Schema(description = "经理抽成比例（占单元楼长分成的比例，0~1不含1）；不传默认 0", example = "0.2000")
    private BigDecimal commissionRate;

    @Schema(description = "状态 active/inactive；不传默认 active", example = "active")
    private String status;
}
