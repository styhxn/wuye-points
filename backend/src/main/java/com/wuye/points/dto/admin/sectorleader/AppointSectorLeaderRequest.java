package com.wuye.points.dto.admin.sectorleader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "任命片区负责人请求")
public class AppointSectorLeaderRequest {

    @NotBlank(message = "residentId不能为空")
    @Schema(description = "业主ID", example = "R001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String residentId;

    @NotBlank(message = "sector不能为空")
    @Schema(description = "板块编码：cleaning/repair/security/greening/other（亦支持中文「保洁/维修/安保/绿化/其它」）",
            example = "cleaning", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sector;

    @Schema(description = "板块描述/备注", example = "负责保洁板块管理")
    private String description;

    @Schema(description = "抽佣比例", example = "0.10")
    private BigDecimal commissionRate;

    @Schema(description = "协调员ID", example = "COORD001")
    private String coordinatorId;

    @Schema(description = "生效日期", example = "2024-01-01")
    private String startDate;

    @Schema(description = "失效日期", example = "2024-12-31")
    private String endDate;

    @Schema(description = "备注", example = "新任命片区负责人")
    private String remark;

    @Schema(description = "状态", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "PENDING"})
    private String status;
}
