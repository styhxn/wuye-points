package com.wuye.points.dto.admin.sectorleader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新板块负责人请求")
public class UpdateSectorLeaderRequest {

    @Schema(description = "业主ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "板块编码：cleaning/repair/security/greening/other（亦支持中文「保洁/维修/安保/绿化/其它」）",
            example = "repair")
    private String sector;

    @Schema(description = "板块描述/备注", example = "负责维修板块管理")
    @Size(max = 200, message = "description长度不能超过200")
    private String description;

    @Schema(description = "抽佣比例", example = "0.10")
    private BigDecimal commissionRate;

    @Schema(description = "统筹负责人ID（统筹提交时会被忽略并强制写为本人）", example = "coo_demo001")
    private String coordinatorId;

    @Schema(description = "生效日期", example = "2024-01-01")
    private String startDate;

    @Schema(description = "失效日期", example = "2024-12-31")
    private String endDate;

    @Schema(description = "备注（不写库，仅兼容旧前端）", example = "更新板块负责人信息")
    private String remark;

    @Schema(description = "状态：active/inactive（大小写不敏感）", example = "active")
    private String status;
}
