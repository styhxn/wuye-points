package com.wuye.points.dto.admin.offlinepoint;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "线下积分发放请求")
public class GrantOfflinePointRequest {

    @Schema(description = "住户ID", example = "RES001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "住户ID不能为空")
    private String residentId;

    @Schema(description = "积分数量", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "积分数量不能为空")
    @Min(value = 1, message = "积分数量至少为1")
    private Integer pointAmount;

    @Schema(description = "积分来源", example = "物业奖励", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "来源不能为空")
    private String source;

    @Schema(description = "积分说明", example = "住户参与社区活动奖励积分", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "说明不能为空")
    private String description;

    @Schema(description = "关联订单ID", example = "ORD20240101001")
    private String relatedOrderId;

    @Schema(description = "操作员备注", example = "特殊情况处理")
    private String operatorRemark;
}
