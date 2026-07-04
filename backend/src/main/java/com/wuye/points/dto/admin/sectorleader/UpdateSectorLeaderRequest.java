package com.wuye.points.dto.admin.sectorleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新片区负责人请求")
public class UpdateSectorLeaderRequest {

    @Schema(description = "业主ID", example = "R001")
    private String residentId;

    @Schema(description = "片区编码", example = "SECTOR_A")
    private String sector;

    @Schema(description = "抽佣比例", example = "0.10")
    private BigDecimal commissionRate;

    @Schema(description = "协调员ID", example = "COORD001")
    private String coordinatorId;

    @Schema(description = "生效日期", example = "2024-01-01")
    private String startDate;

    @Schema(description = "失效日期", example = "2024-12-31")
    private String endDate;

    @Schema(description = "备注", example = "更新片区负责人信息")
    private String remark;

    @Schema(description = "状态", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "PENDING"})
    private String status;
}
