package com.wuye.points.dto.propertyoperator;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePropertyOperatorStatusRequest {

    @NotBlank
    private String status;
}
