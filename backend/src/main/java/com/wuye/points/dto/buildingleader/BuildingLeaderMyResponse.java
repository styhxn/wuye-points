package com.wuye.points.dto.buildingleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "楼长我的信息响应（含分成收益概览）")
public class BuildingLeaderMyResponse {

    @Schema(description = "楼长ID", example = "bl_demo001")
    private String buildingLeaderId;

    @Schema(description = "住户ID", example = "res_xxx")
    private String residentId;

    @Schema(description = "住户姓名", example = "张三")
    private String residentName;

    @Schema(description = "物业公司ID")
    private String propertyCompanyId;

    @Schema(description = "小区ID")
    private String communityId;

    @Schema(description = "小区名称")
    private String communityName;

    @Schema(description = "楼栋", example = "1号楼")
    private String building;

    @Schema(description = "楼长分成比例（占物业公司分成的比例）", example = "0.1000")
    private BigDecimal commissionRate;

    @Schema(description = "状态", example = "active")
    private String status;

    @Schema(description = "累计分成收益（元）", example = "128.00")
    private BigDecimal totalEarning;

    @Schema(description = "已提现完成金额（元）", example = "50.00")
    private BigDecimal withdrawnAmount;

    @Schema(description = "在途提现金额（待审核/已通过，元）", example = "20.00")
    private BigDecimal pendingAmount;

    @Schema(description = "可提现余额（元）", example = "58.00")
    private BigDecimal withdrawableAmount;
}