package com.wuye.points.dto.admin.coinmallrule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "物业币商城规则响应")
public class MallRulesResponse {

    @Schema(description = "物业公司ID", example = "pc_demo001")
    private String propertyCompanyId;

    @Schema(description = "物业币商城是否启用", example = "true")
    private Boolean coinMallEnabled;

    @Schema(description = "物业币抵扣最大比例（0~1）", example = "1.0000")
    private BigDecimal coinMallMaxRatio;

    @Schema(description = "物业币抵扣最低消费金额", example = "0.01")
    private BigDecimal coinMallMinAmount;
}
