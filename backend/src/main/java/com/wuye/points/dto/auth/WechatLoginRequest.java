package com.wuye.points.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "微信登录请求")
public class WechatLoginRequest {

    @NotBlank(message = "code不能为空")
    @Size(max = 256, message = "code长度不能超过256")
    @Schema(description = "微信登录授权码", example = "0a3lGd000abcdef1234", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "加密数据（获取手机号时使用）", example = "EncryptedData...")
    private String encryptedData;

    @Size(max = 24, message = "iv长度不能超过24")
    @Schema(description = "解密向量（获取手机号时使用）", example = "abc123def456")
    private String iv;

    @Size(max = 32, message = "propertyCompanyId长度不能超过32")
    @Schema(description = "物业公司ID", example = "pc_001")
    private String propertyCompanyId;
}
