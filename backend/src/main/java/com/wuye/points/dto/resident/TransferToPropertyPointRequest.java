package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "住户转给物业 - 积分请求")
@Data
public class TransferToPropertyPointRequest {

    @Schema(description = "转出积分数量（>0）", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "pointAmount不能为空")
    @Min(value = 1, message = "pointAmount必须大于0")
    private Integer pointAmount;

    @Schema(description = "备注（可选）", example = "主动捐给物业做活动经费")
    @Size(max = 200, message = "remark长度不能超过200")
    private String remark;
}
