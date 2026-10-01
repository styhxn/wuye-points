package com.wuye.points.dto.companyaccount;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "公司账户流水项响应")
public class CompanyAccountRecordItemResponse {

    @Schema(description = "记录ID", example = "car_xxxxxxxx")
    private String id;

    @Schema(description = "物业公司ID", example = "pc_xxxxxxxx")
    private String propertyCompanyId;

    @Schema(description = "关联订单ID", example = "ord_xxxxxxxx")
    private String orderId;

    @Schema(description = "金额（正数=入账，负数=出账）", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "来源：leader_share/withdrawal_deposit/manual", example = "leader_share")
    private String source;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private String createdAt;
}
