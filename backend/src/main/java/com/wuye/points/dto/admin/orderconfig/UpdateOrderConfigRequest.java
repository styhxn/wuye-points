package com.wuye.points.dto.admin.orderconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端订单配置更新请求
 */
@Data
@Schema(description = "订单配置更新请求")
public class UpdateOrderConfigRequest {

    @Schema(description = "商家核实时限（小时），范围1-168")
    private Integer merchantVerificationHours;

    @Schema(description = "冻结期（天），范围1-30")
    private Integer freezeDays;

    @Schema(description = "退款窗口期（天），范围1-30，不能超过冻结期")
    private Integer refundWindowDays;

    @Schema(description = "是否启用商家核实流程")
    private Boolean merchantVerificationEnabled;
}
