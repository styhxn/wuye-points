package com.wuye.points.dto.admin.sectorleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "片区负责人列表项响应")
public class SectorLeaderListItemResponse {

    @Schema(description = "负责人ID", example = "SL001")
    private String id;

    @Schema(description = "业主ID", example = "R001")
    private String residentId;

    @Schema(description = "业主姓名", example = "张三")
    private String residentName;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "联系电话（与 phone 值一致，保留兼容）", example = "13800138000")
    private String residentPhone;

    @Schema(description = "所属物业公司ID", example = "pc_demo001")
    private String propertyCompanyId;

    @Schema(description = "所属物业公司名称", example = "演示物业公司")
    private String propertyCompanyName;

    @Schema(description = "所属统筹名称（冗余，便于前端展示）", example = "统筹-张伟")
    private String coordinatorName;

    @Schema(description = "片区编码", example = "SECTOR_A")
    private String sector;

    @Schema(description = "片区名称", example = "A区")
    private String sectorName;

    @Schema(description = "板块描述/备注", example = "负责保洁板块管理")
    private String description;

    @Schema(description = "抽佣比例", example = "0.10")
    private BigDecimal commissionRate;

    @Schema(description = "协调员ID", example = "COORD001")
    private String coordinatorId;

    @Schema(description = "下级负责人数量", example = "5")
    private Long individualLeaderCount;

    @Schema(description = "累计收益", example = "12000.00")
    private BigDecimal totalEarnings;

    @Schema(description = "状态（英文码，与 statusCode 一致）", example = "active", allowableValues = {"active", "inactive"})
    private String status;

    @Schema(description = "状态码", example = "active", allowableValues = {"active", "inactive"})
    private String statusCode;

    @Schema(description = "状态中文展示", example = "启用")
    private String statusLabel;

    @Schema(description = "任命时间", example = "2024-01-01 10:00:00")
    private String appointedAt;

    @Schema(description = "创建时间（与 appointedAt 一致）", example = "2024-01-01 10:00:00")
    private String createdAt;
}
