package com.wuye.points.dto.admin.coordinator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "统筹负责人详情响应")
public class CoordinatorDetailResponse {
    @Schema(description = "统筹ID", example = "coo_demo001")
    private String id;
    @Schema(description = "关联住户ID", example = "res_coo01")
    private String residentId;
    @Schema(description = "住户姓名", example = "统筹-张伟")
    private String residentName;
    @Schema(description = "联系电话", example = "13900000002")
    private String residentPhone;
    @Schema(description = "联系电话（旧字段，与 residentPhone 值一致，保留兼容）", example = "13900000002")
    private String phone;
    @Schema(description = "物业公司ID", example = "pc_demo001")
    private String propertyCompanyId;
    @Schema(description = "物业公司名称", example = "演示物业公司")
    private String propertyCompanyName;
    @Schema(description = "统筹描述/备注", example = "负责统筹管理")
    private String description;
    @Schema(description = "佣金比例", example = "0.7")
    private BigDecimal commissionRate;
    @Schema(description = "板块负责人数量", example = "3")
    private Long sectorCount;
    @Schema(description = "板块负责人数量（旧字段，与 sectorCount 值一致）", example = "3")
    private Long sectorLeaderCount;
    @Schema(description = "个体负责人数量", example = "10")
    private Long individualLeaderCount;
    @Schema(description = "管辖范围内商家数量（挂接商家数）", example = "15")
    private Long merchantCount;
    @Schema(description = "累计佣金收入", example = "12500.00")
    private BigDecimal totalEarnings;
    @Schema(description = "状态", example = "active")
    private String status;
    @Schema(description = "创建时间（yyyy-MM-dd HH:mm:ss）", example = "2026-07-03 08:09:15")
    private String createdAt;
    @Schema(description = "任命时间（与 createdAt 一致，保留兼容）", example = "2026-07-03 08:09:15")
    private String appointedAt;
    @Schema(description = "更新时间", example = "2026-07-03 08:09:15")
    private String updatedAt;
}
