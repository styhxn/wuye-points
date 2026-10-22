package com.wuye.points.dto.propertymanager;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "物业经理抽成收益流水项")
public class PropertyManagerEarningItemResponse {

    @Schema(description = "流水ID wf_")
    private String id;

    @Schema(description = "订单ID")
    private String orderId;

    @Schema(description = "分账记录ID dr_")
    private String distributionRecordId;

    @Schema(description = "流水类型 property_manager_share")
    private String entryType;

    @Schema(description = "金额（正数入账，负数冲正）")
    private BigDecimal amount;

    @Schema(description = "余额类型 withdrawable")
    private String balanceType;

    @Schema(description = "冲正关联的原流水ID")
    private String refRecordId;

    @Schema(description = "创建时间")
    private String createdAt;
}
