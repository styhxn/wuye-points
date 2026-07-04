package com.wuye.points.dto.admin.coordinator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "片区协调员列表项响应")
public class CoordinatorListItemResponse {
    @Schema(description = "协调员ID", example = "COO001")
    private String id;
    @Schema(description = "关联住户ID", example = "RES001")
    private String residentId;
    @Schema(description = "住户姓名", example = "张三")
    private String name;
    @Schema(description = "联系电话", example = "13800138000")
    private String phone;
    @Schema(description = "佣金比例", example = "0.05")
    private BigDecimal commissionRate;
    @Schema(description = "片区组长数量", example = "5")
    private Long sectorLeaderCount;
    @Schema(description = "个人组长数量", example = "20")
    private Long individualLeaderCount;
    @Schema(description = "累计佣金收入", example = "12500.00")
    private BigDecimal totalEarnings;
    @Schema(description = "状态", example = "active")
    private String status;
    @Schema(description = "任命时间", example = "2025-01-15T10:00:00")
    private String appointedAt;
}
