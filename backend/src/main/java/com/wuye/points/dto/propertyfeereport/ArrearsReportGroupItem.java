package com.wuye.points.dto.propertyfeereport;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "欠费报表按楼栋分组项")
public class ArrearsReportGroupItem {

    @Schema(description = "楼栋", example = "1栋")
    private String building;

    @Schema(description = "欠费总额", example = "1500.00")
    private BigDecimal totalAmount;

    @Schema(description = "欠费笔数", example = "10")
    private Integer count;
}
