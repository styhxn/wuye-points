package com.wuye.points.dto.admin.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 微信身份认领请求：把来源记录的 openid 迁到目标账号。
 */
@Data
@Schema(description = "微信身份认领请求")
public class ClaimWechatIdentityRequest {

    @Schema(description = "来源记录ID（小程序登录时自动新建、带 openid 的那条住户记录）",
            example = "res_ghost001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "来源记录ID不能为空")
    @Size(max = 32, message = "来源记录ID长度不能超过32")
    private String sourceResidentId;
}
