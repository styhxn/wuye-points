package com.wuye.points.dto.admin.buildingleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "单元楼长列表项响应")
public class BuildingLeaderListItemResponse {

    @Schema(description = "楼长ID", example = "bl_demo001")
    private String id;

    @Schema(description = "关联住户ID", example = "res_xxx")
    private String residentId;

    @Schema(description = "住户姓名", example = "张三")
    private String residentName;

    @Schema(description = "联系电话", example = "13900000000")
    private String residentPhone;

    @Schema(description = "联系电话（旧字段，与 residentPhone 值一致，保留兼容）", example = "13900000000")
    private String phone;

    @Schema(description = "物业公司ID", example = "pc_demo001")
    private String propertyCompanyId;

    @Schema(description = "物业公司名称", example = "演示物业公司")
    private String propertyCompanyName;

    @Schema(description = "小区ID")
    private String communityId;

    @Schema(description = "小区名称")
    private String communityName;

    @Schema(description = "楼栋", example = "1号楼")
    private String building;

    @Schema(description = "楼长分成比例（占物业公司分成的比例）", example = "0.1000")
    private BigDecimal commissionRate;

    @Schema(description = "备注/说明")
    private String description;

    @Schema(description = "累计分成收益（元）", example = "128.00")
    private BigDecimal totalEarning;

    @Schema(description = "状态", example = "active")
    private String status;

    @Schema(description = "创建时间", example = "2026-10-03 08:09:15")
    private String createdAt;

    @Schema(description = "任命时间（与 createdAt 一致，保留兼容）", example = "2026-10-03 08:09:15")
    private String appointedAt;
}