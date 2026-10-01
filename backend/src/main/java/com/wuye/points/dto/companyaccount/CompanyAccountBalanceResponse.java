package com.wuye.points.dto.companyaccount;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "公司账户余额响应")
public class CompanyAccountBalanceResponse {

    @Schema(description = "物业公司ID", example = "pc_xxxxxxxx")
    private String propertyCompanyId;

    @Schema(description = "物业公司名称", example = "示例物业")
    private String propertyCompanyName;

    @Schema(description = "账户余额", example = "12345.67")
    private BigDecimal balance;

    @Schema(description = "最后更新时间", example = "2024-01-01 10:00:00")
    private String lastUpdatedAt;
}
