package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "注销账号请求")
public class RequestAccountDeletionRequest {
    @Schema(description = "确认注销文本", required = true, example = "确认注销")
    @NotBlank(message = "confirmText不能为空")
    private String confirmText;

    @Schema(description = "注销原因", example = "不再使用")
    private String reason;
}