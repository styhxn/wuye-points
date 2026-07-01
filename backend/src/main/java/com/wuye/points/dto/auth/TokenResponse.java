package com.wuye.points.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String tokenType;
}
