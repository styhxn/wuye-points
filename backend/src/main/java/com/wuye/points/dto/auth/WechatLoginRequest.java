package com.wuye.points.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WechatLoginRequest {

    @NotBlank(message = "code不能为空")
    @Size(max = 256, message = "code长度不能超过256")
    private String code;

    private String encryptedData;

    @Size(max = 24, message = "iv长度不能超过24")
    private String iv;

    @Size(max = 32, message = "propertyCompanyId长度不能超过32")
    private String propertyCompanyId;
}
