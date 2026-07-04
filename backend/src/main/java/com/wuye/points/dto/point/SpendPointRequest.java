package com.wuye.points.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "消费积分请求")
public class SpendPointRequest {

    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID", required = true, example = "res_b7m9x1")
    private String residentId;

    @NotNull(message = "积分数量不能为空")
    @Min(value = 1, message = "积分数量至少为1")
    @Schema(description = "积分数量", required = true, example = "50")
    private Integer pointAmount;

    @NotBlank(message = "来源不能为空")
    @Schema(description = "来源", required = true, example = "商品兑换")
    private String source;

    @Size(max = 200, message = "描述长度不能超过200")
    @Schema(description = "描述", example = "兑换商品消耗积分")
    private String description;

    @Schema(description = "来源ID", example = "order_abc123")
    private String sourceId;

    @Schema(description = "家庭ID", example = "fam_abc123")
    private String familyId;

    /** 是否优先使用家庭积分池 */
    @Schema(description = "是否优先使用家庭积分池", example = "false")
    private Boolean useFamilyPool;
}
