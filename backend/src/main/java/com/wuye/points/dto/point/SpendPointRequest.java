package com.wuye.points.dto.point;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SpendPointRequest {

    @NotBlank(message = "用户ID不能为空")
    private String residentId;

    @NotNull(message = "积分数量不能为空")
    @Min(value = 1, message = "积分数量至少为1")
    private Integer pointAmount;

    @NotBlank(message = "来源不能为空")
    private String source;

    @Size(max = 200, message = "描述长度不能超过200")
    private String description;

    private String sourceId;

    private String familyId;

    /** 是否优先使用家庭积分池 */
    private Boolean useFamilyPool;
}
