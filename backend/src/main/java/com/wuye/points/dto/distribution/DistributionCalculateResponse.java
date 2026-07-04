package com.wuye.points.dto.distribution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "分配计算响应")
public class DistributionCalculateResponse {

    @Schema(description = "总金额", example = "1000.00")
    private BigDecimal totalAmount;

    @Schema(description = "佣金比例", example = "0.10")
    private BigDecimal commissionRate;

    @Schema(description = "积分成本", example = "50.00")
    private BigDecimal pointCost;

    @Schema(description = "金币成本", example = "30.00")
    private BigDecimal coinCost;

    @Schema(description = "可分配金额", example = "920.00")
    private BigDecimal distributableAmount;

    @Schema(description = "物业公司份额", example = "460.00")
    private BigDecimal propertyShare;

    @Schema(description = "协调员份额", example = "230.00")
    private BigDecimal coordinatorShare;

    @Schema(description = "片区负责人份额", example = "138.00")
    private BigDecimal sectorLeaderShare;

    @Schema(description = "个人负责人份额", example = "92.00")
    private BigDecimal individualLeaderShare;

    @Schema(description = "协调员ID", example = "coord_abc123")
    private String coordinatorId;

    @Schema(description = "片区负责人ID", example = "sector_xyz789")
    private String sectorLeaderId;

    @Schema(description = "个人负责人ID", example = "ind_def456")
    private String individualLeaderId;
}
