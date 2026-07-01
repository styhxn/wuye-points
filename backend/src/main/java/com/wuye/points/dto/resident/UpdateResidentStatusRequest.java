package com.wuye.points.dto.resident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateResidentStatusRequest {

    @NotBlank(message = "status不能为空")
    @Pattern(regexp = "active|frozen|disabled", message = "status值无效")
    private String status;

    @Size(max = 200, message = "reason长度不能超过200")
    private String reason;
}
