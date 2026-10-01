package com.wuye.points.dto.sectorleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "板块负责人-收入概览")
public class SectorLeaderEarningsResponse {

    @Schema(description = "累计分成收入")
    private BigDecimal totalEarnings;

    @Schema(description = "分成笔数")
    private long recordCount;

    @Schema(description = "分成明细（按时间倒序，最多返回最近200条）")
    private List<SectorLeaderEarningItemResponse> records;
}
