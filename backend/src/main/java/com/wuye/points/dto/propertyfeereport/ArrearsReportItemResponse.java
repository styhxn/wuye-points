package com.wuye.points.dto.propertyfeereport;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@Schema(description = "欠费报表项响应")
public class ArrearsReportItemResponse {

    @Schema(description = "住户ID", example = "res_xxxxxxxx")
    private String residentId;

    @Schema(description = "住户姓名", example = "张三")
    private String residentName;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "楼栋", example = "1栋")
    private String building;

    @Schema(description = "单元", example = "1单元")
    private String unit;

    @Schema(description = "房号", example = "101")
    private String room;

    @Schema(description = "费用类型：property/parking/water/electricity", example = "property")
    private String feeType;

    @Schema(description = "账期", example = "2024-01")
    private String period;

    @Schema(description = "账期起始日", example = "2024-01-01")
    private LocalDate periodStart;

    @Schema(description = "账期结束日", example = "2024-01-31")
    private LocalDate periodEnd;

    @Schema(description = "应缴金额", example = "200.00")
    private BigDecimal amount;

    @Schema(description = "已缴金额", example = "50.00")
    private BigDecimal paidAmount;

    @Schema(description = "欠缴金额", example = "150.00")
    private BigDecimal arrearsAmount;

    @Schema(description = "到期日", example = "2024-01-15")
    private LocalDate dueDate;

    @Schema(description = "逾期天数", example = "15")
    private Integer daysOverdue;

    @Schema(description = "物业费ID", example = "fee_xxxxxxxx")
    private String feeId;
}
