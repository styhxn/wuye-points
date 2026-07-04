package com.wuye.points.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "绑定手机号请求")
public class BindPhoneRequest {

    @NotBlank(message = "code不能为空")
    @Schema(description = "微信登录凭证", required = true, example = "0b1Lm2n3o4P5q6R7s8T")
    private String code;

    @Schema(description = "加密数据", example = "CIPHER_DATA")
    private String encryptedData;

    @Size(max = 24, message = "iv长度不能超过24")
    @Schema(description = "加密初始向量", example = "abc123")
    private String iv;
}
