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

    @Size(max = 256, message = "phoneCode长度不能超过256")
    @Schema(description = "手机号快速验证组件返回的 code（getPhoneNumber 回调里的 code）。"
            + "首次登录（该 openid 未注册过）时必填：缺失返回 30010，前端需拉起手机号授权后重试。"
            + "已有账号（例如管理端预建的商家/负责人账号）会把微信身份绑到该手机号账号上，不新建记录",
            example = "0b3xk0003LmQ...")
    private String phoneCode;
}
