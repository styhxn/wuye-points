package com.wuye.points.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "登录响应")
public class AuthLoginResponse {

    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    @Schema(description = "令牌过期时间（秒）", example = "7200")
    private long expiresIn;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "用户信息")
    private ResidentAuthSummaryResponse resident;
}
