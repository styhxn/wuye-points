package com.wuye.points.dto.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "指派配送请求")
@Data
public class AssignDeliveryRequest {

    @Schema(description = "快递员ID", example = "cour_xxx", required = true)
    @NotBlank(message = "courierId不能为空")
    private String courierId;
}
