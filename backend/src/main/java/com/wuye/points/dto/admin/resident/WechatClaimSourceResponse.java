package com.wuye.points.dto.admin.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 可认领的微信身份来源记录（同手机号、自身带 openid 的其他住户记录）。
 */
@Data
@Builder
@Schema(description = "微信身份认领来源记录")
public class WechatClaimSourceResponse {

    @Schema(description = "来源记录ID（residents.id）", example = "res_ghost001")
    private String residentId;

    @Schema(description = "姓名", example = "王测试")
    private String name;

    @Schema(description = "手机号（脱敏）", example = "138****0008")
    private String phoneMasked;

    @Schema(description = "所属物业公司ID")
    private String propertyCompanyId;

    @Schema(description = "openid 脱敏值，仅供核对是否本人", example = "oUpF****eS6o")
    private String openidMasked;

    @Schema(description = "积分余额（必须为 0 才能认领）", example = "0")
    private Integer pointBalance;

    @Schema(description = "物业币余额（必须为 0 才能认领）", example = "0.00")
    private BigDecimal coinBalance;

    @Schema(description = "是否可直接认领", example = "true")
    private Boolean claimable;

    @Schema(description = "不可认领原因 / 风险提示")
    private String reason;
}
