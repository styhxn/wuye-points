package com.wuye.points.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "获取积分请求")
public class EarnPointRequest {

    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID", required = true, example = "res_b7m9x1")
    private String residentId;

    @NotNull(message = "积分数量不能为空")
    @Min(value = 1, message = "积分数量至少为1")
    @Schema(description = "积分数量", required = true, example = "50")
    private Integer pointAmount;

    @NotBlank(message = "来源不能为空")
    @Schema(description = "来源", required = true, example = "签到")
    private String source;

    @Size(max = 200, message = "描述长度不能超过200")
    @Schema(description = "描述", example = "每日签到获得积分")
    private String description;

    @Schema(description = "来源ID", example = "sign_abc123")
    private String sourceId;

    @Schema(description = "家庭ID", example = "fam_abc123")
    private String familyId;
}
