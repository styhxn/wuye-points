package com.wuye.points.dto.sectorleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "个体负责人简要信息")
public class IndividualLeaderBriefResponse {

    @Schema(description = "个体负责人ID")
    private String id;

    @Schema(description = "业主ID")
    private String residentId;

    @Schema(description = "业主姓名")
    private String residentName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "个体负责人姓名")
    private String name;

    @Schema(description = "板块负责人ID")
    private String sectorLeaderId;

    @Schema(description = "所属板块")
    private String sector;

    @Schema(description = "板块名称")
    private String sectorName;

    @Schema(description = "分成比例")
    private BigDecimal commissionRate;

    @Schema(description = "状态")
    private String status;
}
