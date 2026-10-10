package com.wuye.points.dto.admin.buildingleader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "指定单元楼长请求")
public class AppointBuildingLeaderRequest {

    @NotBlank(message = "residentId不能为空")
    @Schema(description = "住户（楼长）ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "res_xxx")
    private String residentId;

    @Schema(description = "小区ID；不传则取住户所属小区")
    private String communityId;

    @Schema(description = "楼栋；不传则取住户所属楼栋")
    private String building;

    @Schema(description = "楼长分成比例（占物业公司分成的比例，0~1）；不传默认 0.1000", example = "0.1000")
    private BigDecimal commissionRate;

    @Schema(description = "备注/说明", example = "1号楼楼长")
    private String description;

    @Schema(description = "状态 active/inactive；不传默认 active", example = "active")
    private String status;
}