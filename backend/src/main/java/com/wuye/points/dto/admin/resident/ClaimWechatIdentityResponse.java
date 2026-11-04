package com.wuye.points.dto.admin.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 微信身份认领结果。
 */
@Data
@Builder
@Schema(description = "微信身份认领结果")
public class ClaimWechatIdentityResponse {

    @Schema(description = "已绑定微信身份的目标账号ID", example = "res_001")
    private String residentId;

    @Schema(description = "被迁空的来源记录ID", example = "res_ghost001")
    private String sourceResidentId;

    @Schema(description = "openid 脱敏值", example = "oUpF****eS6o")
    private String openidMasked;

    @Schema(description = "目标账号微信绑定状态", example = "true")
    private Boolean wechatBound;

    @Schema(description = "后续动作提示")
    private String message;
}
