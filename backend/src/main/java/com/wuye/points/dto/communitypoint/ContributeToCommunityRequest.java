package com.wuye.points.dto.communitypoint;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "手动注入建设积分请求")
public class ContributeToCommunityRequest {

    @NotBlank(message = "小区ID不能为空")
    @Schema(description = "小区ID", example = "com_abc12345", requiredMode = Schema.RequiredMode.REQUIRED)
    private String communityId;

    @NotNull(message = "金额不能为空")
    @Schema(description = "注入金额", example = "100.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @Schema(description = "备注", example = "订单分成注入建设积分")
    private String remark;
}
