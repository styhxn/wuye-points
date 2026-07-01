package com.wuye.points.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BindPhoneRequest {

    @NotBlank(message = "code不能为空")
    private String code;

    private String encryptedData;

    @Size(max = 24, message = "iv长度不能超过24")
    private String iv;
}
