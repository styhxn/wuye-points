package com.wuye.points.dto.admin.orderconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 管理端订单配置响应
 */
@Data
@Builder
@Schema(description = "订单配置响应（商家核实/冻结期/退款窗口）")
public class OrderConfigResponse {

    @Schema(description = "商家核实时限（小时），默认24")
    private Integer merchantVerificationHours;

    @Schema(description = "冻结期（天），默认7")
    private Integer freezeDays;

    @Schema(description = "退款窗口期（天），默认3，冻结期满后不可退款")
    private Integer refundWindowDays;

    @Schema(description = "是否启用商家核实流程")
    private Boolean merchantVerificationEnabled;
}
