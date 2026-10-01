package com.wuye.points.dto.propertyfeereport;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "欠费报表响应")
public class ArrearsReportResponse {

    @Schema(description = "欠费明细列表")
    private List<ArrearsReportItemResponse> items;

    @Schema(description = "欠费总额", example = "15000.00")
    private BigDecimal totalArrearsAmount;

    @Schema(description = "欠费笔数", example = "100")
    private Integer totalCount;

    @Schema(description = "按楼栋分组汇总")
    private List<ArrearsReportGroupItem> groupByBuilding;
}
