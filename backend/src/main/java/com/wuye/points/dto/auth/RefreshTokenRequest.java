package com.wuye.points.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "刷新令牌请求")
public class RefreshTokenRequest {

    @NotBlank(message = "refreshToken不能为空")
    @Schema(description = "刷新令牌", example = "rt_abc123def456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;
}
