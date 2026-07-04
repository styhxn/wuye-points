package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新住户状态请求")
public class UpdateResidentStatusRequest {

    @NotBlank(message = "status不能为空")
    @Pattern(regexp = "active|frozen|disabled", message = "status值无效")
    @Schema(description = "住户状态", example = "active", allowableValues = {"active", "frozen", "disabled"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;

    @Size(max = 200, message = "reason长度不能超过200")
    @Schema(description = "状态变更原因", example = "用户申请冻结账户")
    private String reason;
}
