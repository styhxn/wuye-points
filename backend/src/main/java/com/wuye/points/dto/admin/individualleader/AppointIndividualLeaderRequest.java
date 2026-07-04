package com.wuye.points.dto.admin.individualleader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "指定个人组长请求")
public class AppointIndividualLeaderRequest {

    @NotBlank(message = "residentId不能为空")
    @Schema(description = "住户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "RES001")
    private String residentId;

    @NotBlank(message = "sectorLeaderId不能为空")
    @Schema(description = "片区组长ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "SL001")
    private String sectorLeaderId;

    @NotBlank(message = "sector不能为空")
    @Schema(description = "所属片区", requiredMode = Schema.RequiredMode.REQUIRED, example = "A片区")
    private String sector;

    @Schema(description = "组长姓名", example = "李四")
    private String name;

    @Schema(description = "佣金比例", example = "0.03")
    private BigDecimal commissionRate;

    @Schema(description = "生效日期", example = "2025-01-01")
    private String startDate;

    @Schema(description = "到期日期", example = "2025-12-31")
    private String endDate;

    @Schema(description = "备注", example = "新任个人组长")
    private String remark;

    @Schema(description = "状态", example = "active")
    private String status;
}
