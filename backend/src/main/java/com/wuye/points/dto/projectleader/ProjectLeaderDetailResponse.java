package com.wuye.points.dto.projectleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "项目负责人详情响应")
public class ProjectLeaderDetailResponse {

    @Schema(description = "项目负责人ID", example = "pl_xxxxxxxx")
    private String id;

    @Schema(description = "业主ID", example = "res_xxxxxxxx")
    private String residentId;

    @Schema(description = "所属区域负责人ID", example = "rl_xxxxxxxx")
    private String regionalLeaderId;

    @Schema(description = "物业公司ID", example = "pc_xxxxxxxx")
    private String propertyCompanyId;

    @Schema(description = "姓名", example = "李四")
    private String name;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "项目名称", example = "示范项目")
    private String projectName;

    @Schema(description = "分成比例", example = "0.1000")
    private BigDecimal shareRate;

    @Schema(description = "状态", example = "active")
    private String status;

    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private String createdAt;

    @Schema(description = "更新时间", example = "2024-01-01 10:00:00")
    private String updatedAt;
}
