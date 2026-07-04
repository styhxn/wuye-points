package com.wuye.points.dto.admin.individualleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新个人组长请求")
public class UpdateIndividualLeaderRequest {
    @Schema(description = "关联住户ID", example = "RES001")
    private String residentId;
    @Schema(description = "片区组长ID", example = "SL001")
    private String sectorLeaderId;
    @Schema(description = "所属片区", example = "A片区")
    private String sector;
    @Schema(description = "组长姓名", example = "李四")
    private String name;
    @Schema(description = "佣金比例", example = "0.03")
    private BigDecimal commissionRate;
    @Schema(description = "生效日期", example = "2025-01-01")
    private String startDate;
    @Schema(description = "到期日期", example = "2025-12-31")
    private String endDate;
    @Schema(description = "备注", example = "调整佣金比例")
    private String remark;
    @Schema(description = "状态", example = "active")
    private String status;
}
