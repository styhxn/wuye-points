package com.wuye.points.dto.admin.offlinepoint;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GrantOfflinePointRequest {

    @NotBlank(message = "住户ID不能为空")
    private String residentId;

    @NotNull(message = "积分数量不能为空")
    @Min(value = 1, message = "积分数量至少为1")
    private Integer pointAmount;

    @NotBlank(message = "来源不能为空")
    private String source;

    @NotBlank(message = "说明不能为空")
    private String description;

    private String relatedOrderId;
    private String operatorRemark;
}
