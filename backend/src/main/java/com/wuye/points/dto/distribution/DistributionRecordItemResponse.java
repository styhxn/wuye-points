package com.wuye.points.dto.distribution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "分配记录项响应")
public class DistributionRecordItemResponse {

    @Schema(description = "记录ID", example = "dist_r7m9x1")
    private String id;

    @Schema(description = "订单ID", example = "ord_k3p5n2")
    private String orderId;

    @Schema(description = "订单编号", example = "ORD20260101001")
    private String orderNo;

    @Schema(description = "商户ID", example = "mer_q2w4e6")
    private String merchantId;

    @Schema(description = "商户名称", example = "好邻居超市")
    private String merchantName;

    @Schema(description = "住户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "住户姓名", example = "张三")
    private String residentName;

    @Schema(description = "物业公司ID", example = "prop_a1c3e5")
    private String propertyCompanyId;

    @Schema(description = "总金额", example = "1000.00")
    private BigDecimal totalAmount;

    @Schema(description = "佣金比例", example = "0.10")
    private BigDecimal commissionRate;

    @Schema(description = "积分成本", example = "50.00")
    private BigDecimal pointCost;

    @Schema(description = "金币成本", example = "30.00")
    private BigDecimal coinCost;

    @Schema(description = "可分配金额", example = "920.00")
    private BigDecimal distributableAmount;

    @Schema(description = "物业公司份额", example = "460.00")
    private BigDecimal propertyShare;

    @Schema(description = "协调员份额", example = "230.00")
    private BigDecimal coordinatorShare;

    @Schema(description = "片区负责人份额", example = "138.00")
    private BigDecimal sectorLeaderShare;

    @Schema(description = "个人负责人份额", example = "92.00")
    private BigDecimal individualLeaderShare;

    @Schema(description = "协调员ID", example = "coord_abc123")
    private String coordinatorId;

    @Schema(description = "协调员姓名", example = "李四")
    private String coordinatorName;

    @Schema(description = "片区负责人ID", example = "sector_xyz789")
    private String sectorLeaderId;

    @Schema(description = "片区负责人姓名", example = "王五")
    private String sectorLeaderName;

    @Schema(description = "个人负责人ID", example = "ind_def456")
    private String individualLeaderId;

    @Schema(description = "个人负责人姓名", example = "赵六")
    private String individualLeaderName;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;
}
