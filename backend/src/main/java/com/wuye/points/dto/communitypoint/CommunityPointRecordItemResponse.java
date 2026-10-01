package com.wuye.points.dto.communitypoint;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "小区建设积分流水项响应")
public class CommunityPointRecordItemResponse {

    @Schema(description = "记录ID", example = "cpr_abc12345")
    private String id;

    @Schema(description = "小区ID", example = "com_abc12345")
    private String communityId;

    @Schema(description = "订单ID", example = "ord_abc12345")
    private String orderId;

    @Schema(description = "金额，正数=注入，负数=消耗", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "来源", example = "order_contribution", allowableValues = {"order_contribution", "manual_adjust"})
    private String source;

    @Schema(description = "备注", example = "订单分成注入建设积分")
    private String remark;

    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private String createdAt;
}
