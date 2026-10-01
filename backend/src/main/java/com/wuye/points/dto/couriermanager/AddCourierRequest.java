package com.wuye.points.dto.couriermanager;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "增加快递员请求")
public class AddCourierRequest {

    @NotBlank(message = "residentId不能为空")
    @Schema(description = "快递员业主ID", example = "res_xxx", requiredMode = Schema.RequiredMode.REQUIRED)
    private String residentId;
}
